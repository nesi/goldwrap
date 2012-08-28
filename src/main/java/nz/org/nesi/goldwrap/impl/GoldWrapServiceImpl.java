package nz.org.nesi.goldwrap.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;
import javax.ws.rs.Path;

import nz.org.nesi.goldwrap.Config;
import nz.org.nesi.goldwrap.api.GoldWrapService;
import nz.org.nesi.goldwrap.domain.Account;
import nz.org.nesi.goldwrap.domain.Allocation;
import nz.org.nesi.goldwrap.domain.ExternalCommand;
import nz.org.nesi.goldwrap.domain.Machine;
import nz.org.nesi.goldwrap.domain.Project;
import nz.org.nesi.goldwrap.domain.User;
import nz.org.nesi.goldwrap.errors.AccountFault;
import nz.org.nesi.goldwrap.errors.AllocationFault;
import nz.org.nesi.goldwrap.errors.GoldCommandException;
import nz.org.nesi.goldwrap.errors.MachineFault;
import nz.org.nesi.goldwrap.errors.ProjectFault;
import nz.org.nesi.goldwrap.errors.ServiceException;
import nz.org.nesi.goldwrap.errors.UserFault;
import nz.org.nesi.goldwrap.util.GoldHelper;
import nz.org.nesi.goldwrap.utils.JSONHelpers;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateMidnight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

@WebService(endpointInterface = "nz.org.nesi.goldwrap.api.GoldWrapService", name = "GoldWrapService")
@Path("/goldwrap")
public class GoldWrapServiceImpl implements GoldWrapService {

	public static Logger myLogger = LoggerFactory
			.getLogger(GoldWrapServiceImpl.class);

	private static volatile boolean initialized = false;

	private static ExternalCommand executeGoldCommand(List<String> command) {
		ExternalCommand gc = new ExternalCommand(command);
		gc.execute();
		gc.verify();
		return gc;
	}

	private static ExternalCommand executeGoldCommand(String command) {
		ExternalCommand gc = new ExternalCommand(command);
		gc.execute();
		gc.verify();
		return gc;
	}

	public GoldWrapServiceImpl() {
		initialize();
	}

	public void addUsersToProject(String projectName, List<User> users) {

		GoldHelper.addUsersToProject(projectName, users);

	}

	public Project addUserToProject(String projName, String userId) {
		return GoldHelper.addUserToProject(projName, userId);
	}

	public void checkMachineName(String machineName) {
		if (StringUtils.isBlank(machineName)) {
			throw new ServiceException("Can't execute operation.",
					"Machine name blank or not specified.");
		}
	}

	public void createMachine(Machine mach) {

		String machName = mach.getName();
		mach.validate(true);

		if (GoldHelper.machineExists(machName)) {
			throw new MachineFault(mach, "Can't create machine " + machName,
					"Machine name '" + machName + "' already exists in Gold.");
		}

		List<String> command = Lists.newArrayList("gmkmachine");

		String desc = mach.getDescription();
		if (StringUtils.isNotBlank(desc)) {
			command.add("-d");
			command.add(desc);
		}

		String arch = mach.getArch();
		if (StringUtils.isNotBlank(arch)) {
			command.add("--arch");
			command.add(arch);

		}

		String opsys = mach.getOpsys();
		if (StringUtils.isNotBlank(opsys)) {
			command.add("--opsys");
			command.add(opsys);

		}

		command.add(machName);

		ExternalCommand ec = executeGoldCommand(command);

		if (!GoldHelper.machineExists(machName)) {
			throw new MachineFault(mach, "Can't create machine.",
					"Unknow reason");
		}

	}

	public void createProject(Project proj) {

		String projName = proj.getProjectId();

		if (StringUtils.isNotBlank(projName)) {
			throw new ProjectFault(
					proj,
					"Can't create project " + projName,
					"Project name will be created internally, you are not allowed to specify it in the request.");
		}

		List<String> indexcommand = Lists.newArrayList("ggetindex");
		ExternalCommand ec_index = executeGoldCommand(indexcommand);

		String lastProj = ec_index.getStdOut().get(0);

		int lastIndex = Integer.parseInt(lastProj.substring(4));

		projName = "nesi" + String.format("%06d", lastIndex + 1);
		proj.setProjectId(projName);

		proj.validate(true);

		if (GoldHelper.projectExists(projName)) {
			throw new ProjectFault(proj, "Can't create project " + projName,
					"Project name '" + projName + "' already exists in Gold.");
		}

		String principal = proj.getPrincipal();
		if (StringUtils.isNotBlank(principal)) {
			try {
				User princ = getUser(principal);
			} catch (Exception e) {
				throw new ProjectFault(proj,
						"Can't create project " + projName, "Principal '"
								+ principal + "' does not exist in Gold.");
			}
		}

		List<User> users = proj.getUsers();
		if (users != null) {
			users = Lists.newArrayList(users);
		} else {
			users = Lists.newArrayList();
		}

		for (User user : users) {
			String userId = user.getUserId();
			if (StringUtils.isBlank(userId)) {
				throw new ProjectFault(proj,
						"Can't create project " + projName,
						"Userid not specified.");
			}
			// if (!GoldHelper.isRegistered(userId)) {
			// throw new ProjectFault(proj,
			// "Can't create project " + projName, "User '" + userId
			// + "' does not exist in Gold yet.");
			// }
		}

		List<String> command = Lists.newArrayList("gmkproject");

		proj.setUsers(new ArrayList<User>());
		proj.setAllocations(new ArrayList<Allocation>());

		String desc = JSONHelpers.convertToJSONString(proj);
		// desc = "{\"projectId\":\"" + projName + "\"}";
		command.add("-d");
		command.add(desc);
		// command.add("'" + desc + "'");

		// String users = Joiner.on(",").join(proj.getUsers());
		//
		// if (StringUtils.isNotBlank(users)) {
		// command.append("-u '" + users + "' ");
		// }

		command.add("--createAccount=False");

		if (proj.isFunded()) {
			command.add("-X");
			command.add("Funded=True");
		} else {
			command.add("-X");
			command.add("Funded=False");
		}

		// String site = proj.getSite();
		// if (StringUtils.isNotBlank(site)) {
		// command.add("-X");
		// command.add("Site=" + site);
		// }

		command.add(projName);

		ExternalCommand ec = executeGoldCommand(command);

		if (!GoldHelper.projectExists(projName)) {
			throw new ProjectFault(proj, "Can't create project.",
					"Unknow reason");
		}

		// myLogger.debug("Creating account...");
		// String command2 = "gmkaccount ";
		//
		// command2 = command2 + "-p " + projName + " ";
		// command2 = command2 + "-n " + "acc_" + projName;
		// ExternalCommand ec2 = executeGoldCommand(command2);
		//
		// int exitCode = ec2.getExitCode();
		// if (exitCode != 0) {
		// try {
		// myLogger.debug("Trying to delete project {}...", projName);
		// deleteProject(projName);
		// } catch (Exception e) {
		// myLogger.debug("Deleting project failed: {}",
		// e.getLocalizedMessage());
		// }
		// throw new ProjectFault(proj, "Could not create project.",
		// "Could not create associated account for some reason.");
		// }
		// myLogger.debug("Parsing output to find out account number.");
		// try {
		// String stdout = ec2.getStdOut().get(0);
		// Iterable<String> tokens = Splitter.on(' ').split(stdout);
		// Integer accNr = Integer.parseInt(Iterables.getLast(tokens));
		// Project tempProj = new Project(projName);
		// tempProj.setAccountId(accNr);
		// // remove ANY user
		// myLogger.debug("Removeing ANY user from account {}", accNr);
		// String removeAnyCommand = "gchaccount --delUsers ANY " + accNr;
		// ExternalCommand removeCommand = executeGoldCommand(removeAnyCommand);
		// modifyProject(projName, tempProj);
		// } catch (Exception e) {
		// try {
		// myLogger.debug("Trying to delete project {}...", projName);
		// deleteProject(projName);
		// } catch (Exception e2) {
		// myLogger.debug("Deleting project failed: {}",
		// e2.getLocalizedMessage());
		// }
		// throw new ProjectFault(proj, "Could not create project.",
		// "Could not parse account nr for project.");
		// }

		myLogger.debug("Account created. Now adding users...");

		GoldHelper.createOrModifyUsers(users);

		addUsersToProject(projName, users);

	}

	public void createUser(User user) {

		GoldHelper.createUser(user);

	}

	public void delete(String resourceType) {

		if ("users".equals(resourceType.toLowerCase())) {

			for (User u : GoldHelper.getAllUsers()) {
				deleteUser(u.getUserId());
			}

		} else if ("projects".equals(resourceType.toLowerCase())) {
			for (Project p : GoldHelper.getAllProjects()) {
				deleteProject(p.getProjectId());
			}
		} else if ("accounts".equals(resourceType.toLowerCase())) {
			for (Account a : GoldHelper.getAllAccounts()) {
				deleteAccount(a.getAccountId());
			}
		}

	}

	public void deleteAccount(Integer accNr) {

		if (!GoldHelper.accountExists(accNr)) {
			throw new AccountFault("Can't delete account " + accNr + ".",
					"Account " + accNr + " not in Gold database.", 404);
		}

		String command = "grmaccount " + accNr;

		ExternalCommand ec = executeGoldCommand(command);

		if (GoldHelper.accountExists(accNr)) {
			throw new AccountFault("Could not delete account " + accNr + ".",
					"Unknown reason", 404);
		}
	}

	public void deleteProject(String projName) {

		GoldHelper.checkProjectname(projName);

		if (!GoldHelper.projectExists(projName)) {
			throw new ProjectFault("Can't delete project " + projName + ".",
					"Project " + projName + " not in Gold database.", 404);
		}

		String command = "grmproject " + projName;
		ExternalCommand ec = executeGoldCommand(command);

		if (GoldHelper.projectExists(projName)) {
			throw new ProjectFault(
					"Could not delete project " + projName + ".",
					"Unknown reason.", 500);
		}
	}

	public void deleteUser(String username) {

		if (StringUtils.isBlank(username)) {
			throw new ServiceException("Can't delete user.",
					"Username blank or not specified.");
		}

		if (!GoldHelper.isRegistered(username)) {
			throw new UserFault("Can't delete user.", "User " + username
					+ " not in Gold database.", 404);
		}

		String command = "grmuser " + username;
		ExternalCommand ec = executeGoldCommand(command);

		if (GoldHelper.isRegistered(username)) {
			throw new UserFault("Could not delete user.", "Unknown reason.",
					500);
		}

	}

	public void deposit(String projName, Allocation alloc) {

		if (alloc == null) {
			throw new AllocationFault("Can't deposit allocation.",
					"No allocation provided", 500);
		}

		alloc.validate(false);

		Project p = getProject(projName);

		String sitename = alloc.getSite();

		Account acc = p.getAccount(sitename);

		DateMidnight start = new DateMidnight(alloc.getStartyear(),
				alloc.getStartmonth(), 1);
		DateMidnight end = null;

		myLogger.debug("Depositing allocation into project " + projName);
		for (int i = 0; i < alloc.getRecharge(); i++) {

			end = start.plusMonths(alloc.getRechargemonths()).minusDays(1);
			myLogger.debug("deposit " + (i + 1) + " for period: {} - {}",
					start.toString(), end.toString());
			List<String> command = Lists.newArrayList("gdeposit");
			command.add("-a");
			command.add(acc.getAccountId().toString());

			String startString = start.getYear() + "-"
					+ String.format("%02d", start.getMonthOfYear()) + "-"
					+ String.format("%02d", start.getDayOfMonth());
			String endString = end.getYear() + "-"
					+ String.format("%02d", end.getMonthOfYear()) + "-"
					+ String.format("%02d", end.getDayOfMonth());

			command.add("-s");
			command.add(startString);
			command.add("-e");
			command.add(endString);
			command.add("-z");

			Integer allocationPerPeriod = alloc.getAllocation()
					/ alloc.getRecharge();

			command.add(allocationPerPeriod.toString());
			command.add("-L");
			command.add(new Integer(allocationPerPeriod * 3).toString());
			command.add("-h");

			ExternalCommand ec = executeGoldCommand(command);

			if (ec.getExitCode() != 0) {
				throw new AllocationFault(alloc, "Could not add allocation.",
						Joiner.on('\n').join(ec.getStdErr()));
			}

			start = end.plusDays(1);
		}
	}

	public Machine getMachine(String machineName) {
		checkMachineName(machineName);

		return GoldHelper.getMachine(machineName);

	}

	public List<Machine> getMachines() {
		return GoldHelper.getAllMachines();
	}

	public Project getProject(String projName) {

		GoldHelper.checkProjectname(projName);

		return GoldHelper.getProject(projName);

	}

	public List<Project> getProjects() {
		return GoldHelper.getAllProjects();
	}

	public List<Project> getProjectsForUser(String username) {
		return GoldHelper.getProjectsForUser(username);
	}

	public List<Project> getProjectsWhereUserIsPrincipal(String username) {
		return GoldHelper.getProjectsWhereUserIsPrincipal(username);
	}

	public User getUser(String username) {

		GoldHelper.checkUsername(username);

		User u = GoldHelper.getUser(username);
		return u;

	}

	public List<User> getUsers() {
		return GoldHelper.getAllUsers();
	}

	public List<User> getUsersForProject(String projName) {
		return GoldHelper.getUsersForProject(projName);
	}

	public synchronized void initialize() {

		if (!initialized) {

			try {

				File configDir = Config.getConfigDir();

				myLogger.debug("Running init commands...");
				File initFile = new File(configDir, "init.config");

				if (initFile.exists()) {
					List<String> lines = null;
					try {
						lines = Files.readLines(initFile, Charsets.UTF_8);
					} catch (IOException e1) {
						throw new RuntimeException("Can't read file: "
								+ initFile.toString());
					}

					for (String line : lines) {
						line = line.trim();
						if (StringUtils.isEmpty(line) || line.startsWith("#")) {
							continue;
						}
						myLogger.debug("Executing: " + line);

						Iterable<String> tokens = Splitter.on(' ')
								.trimResults().split(line);

						List<String> listCommand = Lists.newArrayList(tokens);

						ExternalCommand ec = executeGoldCommand(listCommand);

						myLogger.debug("StdOut:\n\n{}\n\n", Joiner.on("\n")
								.join(ec.getStdOut()));
						myLogger.debug("StdErr:\n\n{}\n\n", Joiner.on("\n")
								.join(ec.getStdErr()));

					}

				}
				myLogger.debug("Trying to initialize static values...");

				File machinesFile = new File(configDir, "machines.json");

				if (machinesFile.exists()) {
					try {
						List<Machine> machines = JSONHelpers.readJSONfile(
								machinesFile, Machine.class);

						for (Machine m : machines) {

							Machine mInGold = null;
							try {
								mInGold = getMachine(m.getName());
								myLogger.debug("Machine " + m.getName()
										+ " in Gold, modifying it...");
								modifyMachine(m.getName(), m);
							} catch (MachineFault mf) {
								myLogger.debug("Machine " + m.getName()
										+ " not in Gold, creating it...");
								createMachine(m);
							} catch (GoldCommandException gce) {
								ExternalCommand ec = (ExternalCommand) gce
										.getFaultInfo().getResource();
								myLogger.debug("Error executing command '"
										+ ec.getCommand() + "': "
										+ ec.getStdErr());
								throw new RuntimeException(
										"Can't modify resource: " + gce);
							}

						}

					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			} finally {
				initialized = true;
			}
		}

	}

	public boolean isRegistered(String user) {
		return GoldHelper.isRegistered(user);
	}

	public Machine modifyMachine(String machName, Machine machine) {

		checkMachineName(machName);

		machine.validate(true);

		Machine mach = null;
		try {
			mach = getMachine(machName);
		} catch (Exception e) {
			myLogger.debug("Can't load machine {}", machName, e);
		}

		if (mach == null) {
			throw new MachineFault("Can't modify machine " + machName + ".",
					"Machine " + machName + " not in Gold database", 404);
		}

		String newArch = machine.getArch();
		String newOs = machine.getOpsys();
		String newDesc = machine.getDescription();

		List<String> command = Lists.newArrayList("gchmachine");
		if (StringUtils.isNotBlank(newDesc)) {
			command.add("-d");
			command.add(newDesc);
		}

		if (StringUtils.isNotBlank(newArch)) {
			command.add("--arch");
			command.add(newArch);
		}

		if (StringUtils.isNotBlank(newOs)) {
			command.add("--opsys");
			command.add(newOs);
		}

		command.add(machName);

		ExternalCommand ec = executeGoldCommand(command);

		return getMachine(machName);

	}

	public Project modifyProject(String projName, Project project) {

		return GoldHelper.modifyProject(projName, project);

	}

	public void modifyUser(String username, User user) {

		GoldHelper.modifyUser(username, user);
	}
}
