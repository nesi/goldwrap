package nz.org.nesi.goldwrap.util


import groovy.util.logging.Slf4j
import nz.org.nesi.goldwrap.domain.Account
import nz.org.nesi.goldwrap.domain.ExternalCommand
import nz.org.nesi.goldwrap.domain.Machine
import nz.org.nesi.goldwrap.domain.Project
import nz.org.nesi.goldwrap.domain.User
import nz.org.nesi.goldwrap.errors.AccountFault
import nz.org.nesi.goldwrap.errors.MachineFault
import nz.org.nesi.goldwrap.errors.ProjectFault
import nz.org.nesi.goldwrap.errors.ServiceException
import nz.org.nesi.goldwrap.errors.UserFault
import nz.org.nesi.goldwrap.utils.BeanHelpers
import nz.org.nesi.goldwrap.utils.JSONHelpers

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils

import com.google.common.base.Splitter
import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import com.google.common.collect.Sets

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
	static final String SITE_KEY = "Site"

	static boolean accountExists(Integer accountId) {
		ExternalCommand gc = executeGoldCommand("glsaccount -show Id -quiet")

		if (gc.getStdOut().contains(accountId.toString())) {
			return true
		} else {
			return false
		}
	}
	public static void addUsersToProject(String projectName, List<User> users) {

		for (User user : users) {

			addUserToProject(projectName, user.getUserId())
		}
	}

	static Project addUserToProject(String projName, String user) {

		if ( ! isRegistered(user) ) {
			throw new UserFault("Can't retrieve user.", "User " + user
			+ " not in Gold database.", 404)
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
			throw new ProjectFault(p, "Could not add user "+user+" to project "+projName, "Unknown reason.", 500)
		}

		List<Integer> accNrs = p.getAccountIds()
		for ( int accNr : accNrs ) {
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
				throw new ProjectFault(p, "Could not add user "+user+" to account "+accNr, "Unknown reason.", 500)
			}
		}

	}

	public static void checkProjectname(String projectname) {
		if (StringUtils.isBlank(projectname)) {
			throw new ServiceException("Can't execute operation.",
			"Projectname blank or not specified.")
		}
	}

	public static void checkUsername(String username) {
		if (StringUtils.isBlank(username)) {
			throw new ServiceException("Can't execute operation.",
			"Username blank or not specified.")
		}
	}

	public static List<String> getAllMachineNames() {

		List<Machine> allMachines = getAllMachines()

		List<String> names = Lists.newLinkedList()

		for (Machine m : allMachines) {
			names.add(m.getName())
		}
		return names

	}

	public static String generateMachinesString(Collection<Machine> machines) {

		List<String> allMachineNames = getAllMachineNames()

		SortedSet<String> string = Sets.newTreeSet()

		for ( Machine m : machines ) {

			if ( ! allMachineNames.contains(m.getName())) {
				throw new MachineFault(m, "Machine "+m.getName()+" not found.", "Machine "+m.getName()+" not in Gold database.")
			}

			string.add(m.getName())
		}

		return StringUtils.join(string, ",")

	}


	private static Account createAccount(Project proj, List<Machine> machines) {

		String machinesString = generateMachinesString(machines)

		String projName = proj.getProjectId()

		log.debug("Creating account...")
		List<String> command2 = Lists.newArrayList()
		command2.add("gmkaccount")
		command2.add("-p")
		command2.add(projName)
		command2.add("-n")
		command2.add(projName+"_"+machinesString)
		command2.add("-m")
		command2.add(machinesString)

		ExternalCommand ec2 = executeGoldCommand(command2)

		int exitCode = ec2.getExitCode()
		if (exitCode != 0) {


			throw new ProjectFault(proj, "Could not create account.",
			"Could not create associated account for some reason.")
		}

		log.debug("Parsing output to find out account number.")
		try {
			String stdout = ec2.getStdOut().get(0)
			Iterable<String> tokens = Splitter.on(' ').split(stdout)
			Integer accNr = Integer.parseInt(Iterables.getLast(tokens))

			proj.addAccountId(accNr)
			// remove ANY user
			log.debug("Removing ANY user from account {}", accNr)
			String removeAnyCommand = "gchaccount --delUsers ANY " + accNr
			ExternalCommand removeCommand = executeGoldCommand(removeAnyCommand)
			modifyProject(projName, proj)
			return getAccount(accNr)
		} catch (Exception e) {
			e.printStackTrace()
			throw new ProjectFault(proj, "Could not create account for project "+proj.getProjectId()+" and machines "+machinesString,
			"Could not parse account nr for project.", e)
		}


	}

	public static void createOrModifyUsers(List<User> users) {
		for (User user : users) {
			if (GoldHelper.isRegistered(user.getUserId())) {
				log.debug("Potentially modifying user " + user.getUserId())
				modifyUser(user.getUserId(), user)
			} else {
				log.debug("Creating user: " + user.getUserId())
				createUser(user)
			}
		}
	}

	public static void createUser(User user) {

		user.validate(false)

		String username = user.getUserId()
		String phone = user.getPhone()
		String email = user.getEmail()

		String middlename = user.getMiddleName()
		String fullname = user.getFirstName()
		if (StringUtils.isNotBlank(middlename)) {
			fullname = fullname + " " + middlename
		}
		fullname = fullname + " " + user.getLastName()

		String institution = user.getInstitution()

		if (isRegistered(username)) {
			throw new UserFault("Can't create user.", "User " + username
			+ " already in Gold database.", 409)
		}

		String desc = JSONHelpers.convertToJSONString(user)

		List<String> command = Lists.newArrayList("gmkuser")
		if (StringUtils.isNotBlank(fullname)) {
			command.add("-n")
			command.add(fullname)
		}
		if (StringUtils.isNotBlank(email)) {
			command.add("-E")
			command.add(email)
		}
		if (StringUtils.isNotBlank(phone)) {
			command.add("-F")
			command.add(phone)
		}

		command.add("-d")
		command.add(desc)
		command.add(username)

		ExternalCommand ec = executeGoldCommand(command)

		if (!GoldHelper.isRegistered(username)) {
			throw new UserFault(user, "Can't create user.", "Unknown reason")
		}

	}

	private static ExternalCommand executeGoldCommand(List<String> command) {
		ExternalCommand gc = new ExternalCommand(command)
		gc.execute()
		gc.verify()
		return gc
	}

	private static ExternalCommand executeGoldCommand(String command) {
		ExternalCommand gc = new ExternalCommand(command)
		gc.execute()
		gc.verify()
		return gc
	}

	public static Account getAccount(Integer accountId) {

		if (! accountExists(accountId)) {
			throw new AccountFault("Account " + accountId + " not found.", "Account "+accountId+" does not exist in Gold.", 404)
		}

		ExternalCommand ec = new ExternalCommand('glsaccount -A --raw '+accountId)
		ec.execute()
		def map = parseGLSOutput(ec.getStdOut())

		if ( map.size() == 0 ) {
			throw new AccountFault("Account " + accountId + " not found.", "Account "+accountId+" does not exist in Gold.", 404)
		}

		if ( map.size() > 1 ) {
			throw new ProjectFault("Multiple accounts with id " + accountId + " found.", "Internal error", 500)
		}

		String key = map.keySet().iterator().next()
		Map value = map.values().iterator().next()

		log.debug('Creating account {}', key)

		Account acc = new Account(accountId)

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

	public static Account getAccount(Project project, List<Machine> machines) {

		String machinesString = generateMachinesString(machines)

		for (Account acc : getAllAccounts(project)) {
			Integer id = acc.getAccountId()

			if (project.getAccountIds().contains(id)) {

				try {
					String accountMachinesString = generateMachinesString(acc.getMachines())

					if ( accountMachinesString.equals(machinesString) ) {
						return acc
					}
				} catch (Exception e) {
					e.printStackTrace()
				}
			}
		}

		log.debug("No account for machines '{}' in project '{}', creating one...",
		machinesString, project.getProjectId())

		Account acc = createAccount(project, machines)
		return acc
	}

	// incomplete, only used for account deletion
	static List<Account> getAllAccounts() {

		ExternalCommand ec = new ExternalCommand('glsaccount -A --raw')
		ec.execute()
		def map = parseGLSOutput(ec.getStdOut(), ID_KEY)

		def accounts = []

		map.each { key, value ->
			log.debug('Creating account object {}', key)

			def id = Integer.parseInt(value[ID_KEY])
			def name = value[NAME_KEY]
			def amount = value[AMOUNT_KEY]
			def projects = value[PROJECTS_KEY]
			def users = value[USERS]
			def machines = value[MACHINES_KEY]
			def desc = value[DESCRIPTION_KEY]
			def site = value[SITE_KEY]

			// we're not filling in the values, we are only interested in the account id
			Account a = new Account(id)
			if (StringUtils.isNotBlank(machines) ) {
				List<Machine> machinesForAccount = Lists.newLinkedList()
				List<String> machinesStrings = machines.split(",")
				for ( String machineName : machinesStrings ) {
					try {
					Machine m = getMachine(machineName)
					machinesForAccount.add(m)
					} catch (Exception e) {
						log.debug("Can't find machine "+machineName+". Ignoring it...")
					}
				}
				a.setMachines(machinesForAccount)
			}
			//			a.setDescription(desc)
			//			a.setSite(site)
			//			a.setP
			accounts.add(a)
		}

		accounts

	}

	static List<Account> getAllAccounts(Project p) {

		List<Account> allAccounts = getAllAccounts()

		List<Account> projAccounts = Lists.newArrayList()
		allAccounts.each { acc ->

			def id = acc.getAccountId()

			if ( p.getAccountIds().contains(id) ) {
				projAccounts.add(acc)
			}

		}

		projAccounts

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

			Project proj
			try {
				proj = JSONHelpers.convertFromJSONString(desc, Project.class)
			} catch (all) {
				log.error("Can't create project "+key+". Ignoring it.")
				return
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
			throw new ProjectFault("Project " + projName + " not found.", "Project "+projName+" does not exist in Gold.", 404)
		}

		ExternalCommand ec = new ExternalCommand('glsproject -A --raw '+projName)
		ec.execute()
		def map = parseGLSOutput(ec.getStdOut())

		if ( map.size() == 0 ) {
			throw new ProjectFault("Project " + projName + " not found.", "Project "+projName+" does not exist in Gold.", 404)
		}

		if ( map.size() > 1 ) {
			throw new ProjectFault("Multiple projects with name " + projName + " found.", "Internal error", 500)
		}

		String key = map.keySet().iterator().next()
		Map value = map.values().iterator().next()

		log.debug('Creating project {}', key)


		def desc = value[DESCRIPTION_KEY]


		Project proj
		try {
			proj = JSONHelpers.convertFromJSONString(desc, Project.class)
		} catch (all) {
			all.printStackTrace()
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
			+ " not in Gold database.", 404)
		}

		def projects = getAllProjects().findAll() { proj ->
			proj.getUsers().contains(username)
		} as List

		return projects
	}

	public static List<Project> getProjectsWhereUserIsPrincipal(String username) {
		if (! isRegistered(username) ) {
			throw new UserFault("Can't retrieve user.", "User " + username
			+ " not in Gold database.", 404)
		}

		def projects = getAllProjects().findAll() { proj ->
			String principal = proj.getPrincipal()
			if ( principal ) {
				return principal.equals(username)
			}
		} as List

		return projects

	}
	static User getUser(String username) {
		ExternalCommand ec = executeGoldCommand("glsuser -show Description "
		+ username + " --quiet")

		if (ec.getStdOut().size() == 0) {
			throw new UserFault("Can't retrieve user.", "User " + username
			+ " not in Gold database.", 404)
		}

		User u = JSONHelpers.extractObject(User.class, ec.getStdOut().get(0))
		if (!username.equals(u.getUserId())) {
			throw new ServiceException("Internal error",
			"Gold userId and userId in description don't match for user '"
			+ username + "'")
		}
		u
	}

	public static List<User> getUsersForProject(String projName) {

		def proj = getProject(projName)

		proj.getUsers()
	}


	static boolean isRegistered(String username) {

		ExternalCommand gc = executeGoldCommand("glsuser -show Name -quiet")

		if (gc.getStdOut().contains(username)) {
			return true
		} else {
			return false
		}
	}


	static boolean machineExists(String machName) {

		ExternalCommand gc = executeGoldCommand("glsmachine -show Name -quiet")

		if (gc.getStdOut().contains(machName)) {
			return true
		} else {
			return false
		}
	}

	static void main(def args) {

		ExternalCommand ec = new ExternalCommand('glsuser -A --raw')
		ec.execute()
		println getAllUsers()
	}

	public static Project modifyProject(String projName, Project project) {

		checkProjectname(projName)

		if (StringUtils.isNotBlank(project.getProjectId())
		&& !projName.equals(project.getProjectId())) {
			throw new ProjectFault(project, "Can't modify project.",
			"Project name can't be changed.")
		}

		if (!projectExists(projName)) {
			throw new ProjectFault("Can't modify project.", "Project "
			+ projName + " not in Gold database.", 404)
		}

		String principal = project.getPrincipal()
		if (StringUtils.isNotBlank(principal)) {
			try {
				User princ = getUser(principal)
			} catch (Exception e) {
				throw new ProjectFault(project, "Can't create project "
				+ projName, "Principal '" + principal
				+ "' does not exist in Gold.")
			}
		}

		List<User> users = project.getUsers()
		if (users != null) {
			users = Lists.newArrayList(users)
		} else {
			users = Lists.newArrayList()
		}

		for (User user : users) {
			String userId = user.getUserId()
			if (StringUtils.isBlank(userId)) {
				throw new ProjectFault(project, "Can't modify project "
				+ projName, "Userid not specified.")
			}

		}

		project.validate(false)

		Project goldProject = getProject(projName)
		try {
			BeanHelpers.merge(goldProject, project)
		} catch (Exception e) {
			e.printStackTrace()
			throw new ProjectFault(project, "Can't modify project " + projName,
			"Can't merge new properties: " + e.getLocalizedMessage())
		}

		// we don't want to store userdata in the description
		goldProject.setUsers(new ArrayList<User>())

		List<String> command = Lists.newArrayList("gchproject")
		String desc = JSONHelpers.convertToJSONString(goldProject)
		command.add("-d")
		command.add(desc)

		if (goldProject.isFunded()) {
			command.add("-X")
			command.add("Funded=True")
		} else {
			command.add("-X")
			command.add("Funded=False")
		}

		// String site = goldProject.getSite();
		// if (StringUtils.isNotBlank(site)) {
		// command.add("-X");
		// command.add("Site=" + site);
		// }

		command.add(projName)

		ExternalCommand ec = executeGoldCommand(command)

		// ensuring users are present
		createOrModifyUsers(users)

		addUsersToProject(projName, users)

		Project p = getProject(projName)
		return p

	}

	public static void modifyUser(String username, User user) {

		if (StringUtils.isBlank(username)) {
			throw new UserFault(user, "Can't modify user.",
			"Username field can't be blank.")
		}

		if (StringUtils.isNotBlank(user.getUserId())
		&& !username.equals(user.getUserId())) {
			throw new UserFault(user, "Can't modify user.",
			"Username can't be changed.")
		}

		if (!isRegistered(username)) {
			throw new UserFault("Can't modify user.", "User " + username
			+ " not in Gold database.", 404)
		}

		User goldUser = getUser(username)
		try {
			BeanHelpers.merge(goldUser, user)
		} catch (Exception e) {
			throw new UserFault(goldUser, "Can't merge new user into old one.",
			e.getLocalizedMessage())
		}

		goldUser.validate(false)

		String middlename = goldUser.getMiddleName()
		String fullname = goldUser.getFirstName()
		if (StringUtils.isNotBlank(middlename)) {
			fullname = fullname + " " + middlename
		}
		fullname = fullname + " " + goldUser.getLastName()
		String phone = goldUser.getPhone()
		String institution = goldUser.getInstitution()
		String email = goldUser.getEmail()

		String desc = JSONHelpers.convertToJSONString(goldUser)

		List<String> command = Lists.newArrayList("gchuser")
		if (StringUtils.isNotBlank(fullname)) {
			command.add("-n")
			command.add("fullname")
		}
		if (StringUtils.isNotBlank(email)) {
			command.add("-E")
			command.add(email)
		}
		if (StringUtils.isNotBlank(phone)) {
			command.add("-F")
			command.add(phone)
		}
		command.add("-d")
		command.add(desc)
		command.add(username)

		ExternalCommand ec = executeGoldCommand(command)

		if (!GoldHelper.isRegistered(username)) {
			throw new UserFault(goldUser, "Can't create user.",
			"Unknown reason")
		}

	}


	static def parseGLSOutput(def output) {
		return parseGLSOutput(output, NAME_KEY)
	}

	static def parseGLSOutput(def output, String KEY) {

		output = output.findAll {
			( ! it.trim().startsWith("-") ) && ! (it.trim().startsWith("root") )
		}
		def keyList = null
		try {
			keyList = Lists.newArrayList(
			Splitter.on('|').trimResults().split(output.get(0)))
			output.remove(0)
		} catch (IndexOutOfBoundsException ioobe) {
			log.debug('No data')
			return [:]
		}

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

		ExternalCommand gc = executeGoldCommand("glsproject -show Name -quiet")

		if (gc.getStdOut().contains(projName)) {
			return true
		} else {
			return false
		}
	}
}
