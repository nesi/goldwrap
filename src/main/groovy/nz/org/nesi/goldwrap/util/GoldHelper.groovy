package nz.org.nesi.goldwrap.util


import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils


import groovy.util.logging.Slf4j

import com.google.common.base.Splitter
import com.google.common.collect.Lists

import nz.org.nesi.goldwrap.domain.Account
import nz.org.nesi.goldwrap.domain.ExternalCommand
import nz.org.nesi.goldwrap.domain.Machine
import nz.org.nesi.goldwrap.domain.Project
import nz.org.nesi.goldwrap.domain.User
import nz.org.nesi.goldwrap.errors.AccountFault
import nz.org.nesi.goldwrap.errors.MachineFault
import nz.org.nesi.goldwrap.errors.ProjectFault;
import nz.org.nesi.goldwrap.errors.ServiceException;
import nz.org.nesi.goldwrap.errors.UserFault
import nz.org.nesi.goldwrap.utils.JSONHelpers

@Slf4j
class GoldHelper {

	static final String ANY_KEY = "ANY"

	static final String NAME_KEY = "Name"
	static final String DESCRIPTION_KEY = "Description"
	static final String USERS = "Users"
	static final String ACTIVE_KEY = "Active"
	static final String ARCHITECTURE_KEY = "Architecture"
	static final String OPERATING_SYSTEM_KEY = "OperatingSystem"
	static final String ORGANIZATION_KEY = "Organization"
	static final String ID_KEY = "Id"
	static final String AMOUNT_KEY = "Amount"
	static final String PROJECTS_KEY = "Projects"
	static final String MACHINES_KEY = "Machines"

	static boolean accountExists(Integer accountId) {
		ExternalCommand gc = executeGoldCommand("glsaccount -show Id -quiet");

		if (gc.getStdOut().contains(accountId.toString())) {
			return true;
		} else {
			return false;
		}
	}
	static Project addUserToProject(String projName, String user) {

		if ( ! isRegistered(user) ) {
			throw new UserFault("Can't retrieve user.", "User " + user
			+ " not in Gold database.", 404);
		}

		log.debug("Adding user "+user+" to project "+projName)
		ExternalCommand ec = new ExternalCommand('gchproject --addUser '+user+ " "+projName)
		ec.execute()

		Project p = getProject(projName)

		User tmp = p.getUsers().findResult { it ->
			if ( it.getUserId().equals(user) ) {
				return it
			}
		}

		if (! tmp) {
			throw new ProjectFault(p, "Could not add user "+user+" to project "+projName, "Unknown reason.", 500);
		}

		int accNr = p.getAccountId()
		if ( ! accNr || accNr <= 0 ) {
			throw new ProjectFault(p, "Could not find account number for project "+projName)
		}
		log.debug("Adding user "+user+" to account "+accNr)

		ExternalCommand ec2 = new ExternalCommand('gchaccount --addUser '+user+' '+accNr)
		ec2.execute()

		// checking whether user was added to account
		Account acc = getAccount(accNr)

		User tmp2 = acc.getUsers().findResult { it ->
			if ( it.getUserId().equals(user) ) {
				return it
			}
		}

		if (! tmp2) {
			throw new ProjectFault(p, "Could not add user "+user+" to account "+accNr, "Unknown reason.", 500);
		}

	}

	private static ExternalCommand executeGoldCommand(String command) {
		ExternalCommand gc = new ExternalCommand(command);
		gc.execute();
		gc.verify();
		return gc;
	}

	public static Account getAccount(Integer accountId) {

		if (! accountExists(accountId)) {
			throw new AccountFault("Account " + accountId + " not found.", "Account "+accountId+" does not exist in Gold.", 404);
		}

		ExternalCommand ec = new ExternalCommand('glsaccount -A --raw '+accountId)
		ec.execute()
		def map = parseGLSOutput(ec.getStdOut())

		if ( map.size() == 0 ) {
			throw new AccountFault("Account " + accountId + " not found.", "Account "+accountId+" does not exist in Gold.", 404);
		}

		if ( map.size() > 1 ) {
			throw new ProjectFault("Multiple accounts with id " + accountId + " found.", "Internal error", 500);
		}

		String key = map.keySet().iterator().next()
		Map value = map.values().iterator().next()

		log.debug('Creating account {}', key)

		Account acc = new Account(accountId);

		def usersString = value[USERS]
		def users = usersString.split (',') as List

		def result = []
		if ( users.contains(ANY_KEY)) {
			result = getAllUsers()
		} else {

			users.each { it ->
				try {
					User u = getUser(it)
					result.add(u)
				}  catch (all) {
					UserFault f = new UserFault("Can't load user '"+it+"'.", "Error retrieving user '"+it+"' from Gold.", 500)
					f.getFaultInfo().setException(ExceptionUtils.getStackTrace(all))
					throw f
				}
			}
		}
		acc.setUsers(result)

		def resultP = []
		def projectsString = value[PROJECTS_KEY]
		def projects = projectsString.split (',') as List
		if ( projects.contains(ANY_KEY)) {
			resultP = getAllProjects()
		} else {
			projects.each { it ->
				try {
					Project p = getProject(it)
					resultP.add(p)
				}  catch (all) {
					ProjectFault f = new ProjectFault("Can't load project '"+it+"'.", "Error retrieving project '"+it+"' from Gold.", 500)
					f.getFaultInfo().setException(ExceptionUtils.getStackTrace(all))
					throw f
				}
			}
		}
		acc.setProjects(resultP)


		def desc = value[DESCRIPTION_KEY]
		acc.setDescription(desc)
		return acc
	}


	// incomplete, only used for account deletion
	static List<Account> getAllAccounts() {

		ExternalCommand ec = new ExternalCommand('glsaccount -A --raw')
		ec.execute()
		def map = parseGLSOutput(ec.getStdOut(), ID_KEY)

		def accounts = []

		map.each { key, value ->
			log.debug('Creating machine {}', key)

			def id = Integer.parseInt(value[ID_KEY])
			def name = value[NAME_KEY]
			def amount = value[AMOUNT_KEY]
			def projects = value[PROJECTS_KEY]
			def users = value[USERS]
			def machines = value[MACHINES_KEY]
			def desc = value[DESCRIPTION_KEY]

			Account a = new Account(id)
			accounts.add(a)
		}

		accounts

	}

	static List<Machine> getAllMachines() {

		ExternalCommand ec = new ExternalCommand('glsmachine -A --raw')
		ec.execute()
		def map = parseGLSOutput(ec.getStdOut())

		def machines = []
		map.each { key, value ->
			log.debug('Creating machine {}', key)

			def desc = value[DESCRIPTION_KEY]
			def arch = value[ARCHITECTURE_KEY]
			def opsys = value[OPERATING_SYSTEM_KEY]

			Machine m = new Machine(key)
			m.setArch(arch)
			m.setOpsys(opsys)
			m.setDescription(desc)

			machines.add(m)
		}
		machines
	}

	static List<Project> getAllProjects() {
		ExternalCommand ec = new ExternalCommand('glsproject -A --raw')
		ec.execute()
		def map = parseGLSOutput(ec.getStdOut())

		def projects = []
		map.each { key, value ->
			log.debug('Creating project {}', key)

			def desc = value[DESCRIPTION_KEY]

			Project proj;
			try {
				proj = JSONHelpers.convertFromJSONString(desc, Project.class)
			} catch (all) {
				log.error("Can't create project "+key+". Ignoring it.")
				return
			}


			def users = value[USERS].split (',') as List
			proj.setUsers(users)

			projects.add(proj)
		}

		projects
	}

	static List<User> getAllUsers() {

		ExternalCommand ec = new ExternalCommand('glsuser -A --raw')
		ec.execute()
		def map = parseGLSOutput(ec.getStdOut())

		def users = []
		map.each { key, value ->
			log.debug('Creating user {}', key)
			def desc = value[DESCRIPTION_KEY]
			if ( ! desc ) {
				log.debug('Ignoring user {}', key)
			} else {
				try {
					User u = JSONHelpers.convertFromJSONString(desc, User.class)
					users.add(u)
				} catch (all) {
					log.error ('Could not import user {}: {}', key, all)
				}
			}
		}
		users
	}

	static Machine getMachine(String machName) {
		ExternalCommand ec = executeGoldCommand("glsmachine "
				+ machName + " --raw")

		if (ec.getStdOut().size() == 0) {
			throw new MachineFault("Can't retrieve machine.", "Machine " + machName
			+ " not in Gold database.", 404)
		}

		def map = parseGLSOutput(ec.getStdOut())

		if (map.size() != 1) {
			throw new MachineFault("Can't find unique machine with name "
			+machName, "Machine "+machName+ "not in Gold database or multiple results found.", 404)
		}

		def machineProps = map[machName]
		def arch = machineProps[ARCHITECTURE_KEY]
		def opsys = machineProps[OPERATING_SYSTEM_KEY]
		def desc = machineProps[DESCRIPTION_KEY]

		Machine m = new Machine(machName)
		m.setArch(arch)
		m.setOpsys(opsys)
		m.setDescription(desc)
		m
	}

	public static Project getProject(String projName) {

		if (! projectExists(projName)) {
			throw new ProjectFault("Project " + projName + " not found.", "Project "+projName+" does not exist in Gold.", 404);
		}

		ExternalCommand ec = new ExternalCommand('glsproject -A --raw '+projName)
		ec.execute()
		def map = parseGLSOutput(ec.getStdOut())

		if ( map.size() == 0 ) {
			throw new ProjectFault("Project " + projName + " not found.", "Project "+projName+" does not exist in Gold.", 404);
		}

		if ( map.size() > 1 ) {
			throw new ProjectFault("Multiple projects with name " + projName + " found.", "Internal error", 500);
		}

		String key = map.keySet().iterator().next()
		Map value = map.values().iterator().next()

		log.debug('Creating project {}', key)


		def desc = value[DESCRIPTION_KEY]


		Project proj;
		try {
			proj = JSONHelpers.convertFromJSONString(desc, Project.class)
		} catch (all) {
			all.printStackTrace();
			throw new ProjectFault("Can't create project "+key+".", "Can't read description field for project "+key, 500)
		}

		String usersString = value[USERS]
		if ( usersString) {
			usersString = usersString.trim()
			def users = usersString.split (',') as List
			if ( users ) {
				def result = []
				users.each { it ->
					try {
						User u = getUser(it)
						result.add(u)
					}  catch (all) {
						UserFault f = new UserFault("Can't load user '"+it+"'.", "Error retrieving user '"+it+"' from Gold.", 500)
						f.getFaultInfo().setException(ExceptionUtils.getStackTrace(all))
						throw f
					}
				}
				proj.setUsers(result)
			}
		}

		return proj
	}

	public static List<Project> getProjectsForUser(String username) {

		if (! isRegistered(username) ) {
			throw new UserFault("Can't retrieve user.", "User " + username
			+ " not in Gold database.", 404);
		}

		def projects = getAllProjects().findAll() { proj ->
			proj.getUsers().contains(username)
		} as List

		return projects
	}

	public static List<Project> getProjectsWhereUserIsPrincipal(String username) {
		if (! isRegistered(username) ) {
			throw new UserFault("Can't retrieve user.", "User " + username
			+ " not in Gold database.", 404);
		}

		def projects = getAllProjects().findAll() { proj ->
			proj.getPrincipal().equals(username)
		} as List

		return projects

	}

	static User getUser(String username) {
		ExternalCommand ec = executeGoldCommand("glsuser -show Description "
				+ username + " --quiet");

		if (ec.getStdOut().size() == 0) {
			throw new UserFault("Can't retrieve user.", "User " + username
			+ " not in Gold database.", 404);
		}

		User u = JSONHelpers.extractObject(User.class, ec.getStdOut().get(0));
		if (!username.equals(u.getUserId())) {
			throw new ServiceException("Internal error",
			"Gold userId and userId in description don't match for user '"
			+ username + "'");
		}
		u
	}

	public static List<User> getUsersForProject(String projName) {

		def proj = getProject(projName)

		proj.getUsers()
	}

	static boolean isRegistered(String username) {

		ExternalCommand gc = executeGoldCommand("glsuser -show Name -quiet");

		if (gc.getStdOut().contains(username)) {
			return true;
		} else {
			return false;
		}
	}

	static boolean machineExists(String machName) {

		ExternalCommand gc = executeGoldCommand("glsmachine -show Name -quiet");

		if (gc.getStdOut().contains(machName)) {
			return true;
		} else {
			return false;
		}
	}

	static void main(def args) {

		ExternalCommand ec = new ExternalCommand('glsuser -A --raw')
		ec.execute()
		println getAllUsers()
	}

	static def parseGLSOutput(def output) {
		return parseGLSOutput(output, NAME_KEY)
	}

	static def parseGLSOutput(def output, String KEY) {

		output = output.findAll {
			( ! it.trim().startsWith("-") ) && ! (it.trim().startsWith("root") )
		}

		def keyList = Lists.newArrayList(
				Splitter.on('|').trimResults().split(output.get(0)))
		output.remove(0)

		def result = [:]

		for ( def line : output ) {
			List tokens = Lists.newArrayList(
					Splitter.on('|').trimResults().split(line))

			def map = [keyList, tokens].transpose().collectEntries{ it }
			def name = map.get(KEY)
			if ( name ) {
				result[map.get(KEY)] = map
			}
		}
		result
	}
	static boolean projectExists(String projName) {

		ExternalCommand gc = executeGoldCommand("glsproject -show Name -quiet");

		if (gc.getStdOut().contains(projName)) {
			return true;
		} else {
			return false;
		}
	}
}
