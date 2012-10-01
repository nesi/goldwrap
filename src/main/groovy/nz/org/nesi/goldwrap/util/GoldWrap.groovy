package nz.org.nesi.goldwrap.util

import groovy.util.logging.Slf4j
import nz.org.nesi.goldwrap.Config
import nz.org.nesi.goldwrap.domain.Allocation
import nz.org.nesi.goldwrap.domain.ExternalCommand
import nz.org.nesi.goldwrap.domain.Machine
import nz.org.nesi.goldwrap.domain.Organization
import nz.org.nesi.goldwrap.domain.Project
import nz.org.nesi.goldwrap.domain.User
import nz.org.nesi.goldwrap.errors.AccountFault
import nz.org.nesi.goldwrap.errors.AllocationFault
import nz.org.nesi.goldwrap.errors.MachineFault
import nz.org.nesi.goldwrap.errors.OrganizationFault
import nz.org.nesi.goldwrap.errors.ProjectFault
import nz.org.nesi.goldwrap.errors.UserFault

import org.apache.commons.lang3.StringUtils
import org.joda.time.DateMidnight

import com.google.common.base.Joiner
import com.google.common.base.Splitter
import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import com.google.common.collect.Sets

@Slf4j
class GoldWrap {

	static final String ANY_KEY = "ANY"


	static final String NAME_KEY = "Name"

	static final String DESCRIPTION_KEY = "Description"
	static final String USERS_KEY = "Users"
	static final String ACTIVE_KEY = "Active"
	static final String ARCHITECTURE_KEY = "Architecture"
	static final String OPERATING_SYSTEM_KEY = "OperatingSystem"
	static final String ORGANIZATION_KEY = "Organization"
	static final String ID_KEY = "Id"
	static final String AMOUNT_KEY = "Amount"
	static final String PROJECTS_KEY = "Projects"
	static final String MACHINES_KEY = "Machines"
	static final String SITE_KEY = "Site"
	static final String PHONE_KEY = "PhoneNumber"
	static final String EMAIL_KEY = "EmailAddress"
	static final String FULLNAME_KEY = "CommonName"
	static final String AFFILIATION_KEY = "Affiliation"


	public static void addAllocationToProject(String projectId, Allocation alloc) {

		Project proj = getProject(projectId)

		int acc_id = createAccountAndChangeProject(proj, alloc)

		DateMidnight start = new DateMidnight(alloc.getStartyear(), 	alloc.getStartmonth(), 1)
		DateMidnight end = null

		log.debug("Depositing allocation into project " + projectId)

		for (int i = 0; i < alloc.getRecharge(); i++) {

			end = start.plusMonths(alloc.getRechargemonths()).minusDays(1)
			log.debug("deposit " + (i + 1) + " for period: {} - {}", start.toString(), end.toString())
			List<String> depositcommand = Lists.newArrayList("gdeposit")
			depositcommand.add("-a")
			depositcommand.add(Integer.toString(acc_id))

			String startString = start.getYear() + "-" + String.format("%02d", start.getMonthOfYear()) + "-" + String.format("%02d", start.getDayOfMonth())
			String endString = end.getYear() + "-" + String.format("%02d", end.getMonthOfYear()) + "-" + String.format("%02d", end.getDayOfMonth())

			depositcommand.add("-s")
			depositcommand.add(startString)
			depositcommand.add("-e")
			depositcommand.add(endString)
			depositcommand.add("-z")

			Integer allocationPerPeriod = alloc.getAllocation() / alloc.getRecharge()

			depositcommand.add(allocationPerPeriod.toString())
			depositcommand.add("-L")
			depositcommand.add(new Integer(allocationPerPeriod * 3).toString())
			depositcommand.add("-h")

			// String clazz = alloc.getClazz();
			// depositcommand.add("-X");
			// depositcommand.add("Class=" + clazz);

			ExternalCommand ec = executeGoldCommand(depositcommand)

			if (ec.getExitCode() != 0) {
				throw new AllocationFault(alloc, "Could not add allocation.",
				Joiner.on('\n').join(ec.getStdErr()));
			}

			start = end.plusDays(1)

		}

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

	public static List<String> getAllMachineNames() {

		List<Machine> allMachines = getAllMachines()

		List<String> names = Lists.newLinkedList()

		for (Machine m : allMachines) {
			names.add(m.getName())
		}
		return names

	}

	/**
	 * Creates a new account for each new allocation.
	 *
	 * @param proj
	 * @param alloc
	 * @return
	 */
	private static int createAccountAndChangeProject(Project proj, Allocation alloc) {

		List<String> machines = alloc.getMachines()

		for ( String m : machines ) {
			if ( !machineExists(m) ) {
				throw new AllocationFault("Can't add allocation.", "Machine "+m+" doesn't exist in Gold database.", 400);
			}
		}

		String projName = proj.getProjectId()

		log.debug("Creating account...")
		List<String> command2 = Lists.newArrayList()
		command2.add("gmkaccount")
		command2.add("-p")
		command2.add(projName)
		command2.add("-n")
		command2.add(projName+"_"+StringUtils.join(machines, '_'))
		command2.add("-m")
		command2.add(StringUtils.join(machines, ','))

		ExternalCommand ec2 = executeGoldCommand(command2)

		int exitCode = ec2.getExitCode()
		if (exitCode != 0) {


			throw new AccountFault("Could not create account.",
			"Could not create associated account for some reason.", 500)
		}

		log.debug("Parsing output to find out account number.")
		try {
			String stdout = ec2.getStdOut().get(0)
			Iterable<String> tokens = Splitter.on(' ').split(stdout)
			Integer accNr = Integer.parseInt(Iterables.getLast(tokens))

			// remove ANY user
			log.debug("Removing ANY user from account {}", accNr)
			String removeAnyCommand = "gchaccount --delUsers ANY " + accNr
			ExternalCommand removeCommand = executeGoldCommand(removeAnyCommand)
			proj.setMachines(machines)
			modifyProject(projName, machines, alloc.getClazz(), "")
			return accNr
		} catch (Exception e) {
			e.printStackTrace()
			throw new ProjectFault(proj, "Could not create account for project "+proj.getProjectId()+" and machines "+StringUtils.join(machines, ','),
			"Could not parse account nr for project.", e)
		}

	}


	public static void addUserToProject(String projectId, String username) {

		if ( ! isRegistered(username) ) {
			throw new UserFault("Can't retrieve user.", "User " + username
			+ " not in Gold database.", 404)
		}

		if ( ! projectExists(projectId) ) {
			throw new ProjectFault("Can't find project.", "Project "+projectId+" not in Gold database.", 404)
		}

		log.debug("Adding user "+username+" to project "+projectId)
		ExternalCommand ec = executeGoldCommand('gchproject --addUser '+username+ " "+projectId)

		Project p = getProject(projectId)

		if (! p.getUsers().contains(username)) {
			throw new ProjectFault("Could not add user "+username+" to project "+projectId+".", "Unknown reason", 500)
		}
	}

	public static void createMachine(String name, String arch, String os, String desc) {

		if (StringUtils.isBlank(name)) {
			throw new MachineFault("Can't create machine.", "No machine name specified.", 400);
		}

		List<String> command = Lists.newArrayList("gmkmachine")
		if (StringUtils.isNotBlank(arch)) {
			command.add('--arch')
			command.add(arch)
		}
		if (StringUtils.isNotBlank(os)) {
			command.add('--opsys')
			command.add(os)
		}
		if (StringUtils.isNotBlank(desc)) {
			command.add('-d')
			command.add(desc)
		}

		command.add(name)

		ExternalCommand ec = executeGoldCommand(command)

		if (!GoldWrap.machineExists(name)) {
			throw new MachineFault("Can't create machine "+name+".", "Unknown reason", 500)
		}
	}

	public static void createOrganization(String name, String desc) {

		if ( StringUtils.isBlank(name) ) {
			throw new OrganizationFault("Can't create organization.", "Organization name not specified", 400)
		}

		if ( organizationExists(name) ) {
			throw new OrganizationFault("Can't create organization.", "Organization with id "+name+" already exists.")
		}

		List<String> command = Lists.newArrayList("goldsh")
		command.add("Organization")
		command.add("Create")

		command.add("Name=\""+name+"\"")

		if (StringUtils.isNotBlank(desc)) {
			command.add("Description=\""+desc+"\"")
		}

		ExternalCommand ec = executeGoldCommand(command)

		if (!GoldWrap.organizationExists(name)) {
			throw new OrganizationFault("Can't create organization "+name+".", "Unknown reason", 500)
		}
	}

	public static void createProject(String projectId, List<String> machines, String description) {

		if ( StringUtils.isBlank(projectId) ) {
			throw new ProjectFault("Can't create project.", "ProjectId not specified", 400)
		}

		if ( projectExists(projectId) ) {
			throw new ProjectFault("Can't create project.", "Project with id "+projectId+" already exists.")
		}

		List<String> command = Lists.newArrayList("gmkproject")

		if (StringUtils.isNotBlank(description)) {
			command.add("-d")
			command.add(description)
		}
		command.add("--createAccount=False")

		if ( machines ) {
			for ( String m : machines ) {
				if (! machineExists(m)) {
					throw new ProjectFault("Can't create project "+projectId, "Machine "+m+" does not exist in Gold.", 400)
				}
			}
			command.add("-m")
			command.add(StringUtils.join(machines, ','))
		}

		command.add(projectId)

		ExternalCommand ec = executeGoldCommand(command)

		if (!GoldWrap.projectExists(projectId)) {
			throw new ProjectFault("Can't create project "+projectId+".", "Unknown reason", 500)
		}
	}

	public static void createUser(String username, String fullName, String institution, String affiliation, String email, String phone) {

		if ( StringUtils.isBlank(username) ) {
			throw new UserFault("Can't create user.", "Username not specified", 400)
		}

		if ( StringUtils.isBlank(fullName) ) {
			throw new UserFault("Can't create user.", "Full name not specified", 400)
		}

		if ( StringUtils.isBlank(email) ) {
			throw new UserFault("Can't create user.", "Email address not specified", 400)
		}

		if ( StringUtils.isBlank(institution) ) {
			throw new UserFault("Can't create user.", "Organization not specified", 400)
		}


		if (isRegistered(username)) {
			throw new UserFault("Can't create user.", "User " + username
			+ " already in Gold database.", 409)
		}

		if (!organizationExists(institution)) {
			createOrganization(institution, "");
		}


		List<String> command = Lists.newArrayList("gmkuser")
		if (StringUtils.isNotBlank(fullName)) {
			command.add("-n")
			command.add(fullName)
		}
		if (StringUtils.isNotBlank(email)) {
			command.add("-E")
			command.add(email)
		}
		if (StringUtils.isNotBlank(phone)) {
			command.add("-F")
			command.add(phone)
		}

		if (StringUtils.isNotBlank(affiliation)) {
			command.add("--extension")
			command.add("Affiliation="+affiliation)
		}

		command.add("--extension")
		command.add("Organization="+institution)

		command.add(username)

		ExternalCommand ec = executeGoldCommand(command)

		if (!GoldWrap.isRegistered(username)) {
			throw new UserFault("Can't create user "+username+".", "Unknown reason", 500)
		}
	}

	public static void deleteProject(String id) {
		ExternalCommand ec = executeGoldCommand("grmproject "+id)

		if ( projectExists(id) ) {
			throw new ProjectFault("Could not delete project "+id+".", "Unknown reason", 500)
		}
	}

	public static void deleteUser(String username) {

		ExternalCommand ec = executeGoldCommand("grmuser "+username)

		if ( isRegistered(username) ) {
			throw new UserFault("Could not delete user "+username+".", "Unknown reason")
		}
	}

	private static void execute(ExternalCommand ec) {

		if ( ec.getExecuted() ) {
			throw new RuntimeException("Command already executed.")
		}

		ec.setExecuted(new Date())
		Process proc = null

		log.debug("Executing command..."+ec.toString())
		List<String> commandToExecute = []
		// checking whether we can use gold directly
		if ( Config.getCommandPrefix() ) {
			Iterable config = Splitter.on(' ').split(Config.getCommandPrefix())
			config = Lists.newArrayList(config)
			def last = config.last()
			config.pop()
			def first = ec.getCommand().first()
			def joined = last+first
			ec.getCommand().remove(0)

			commandToExecute = [
				config,
				joined,
				ec.getCommand()
			].flatten()

		} else {
			commandToExecute = ec.getCommand()
		}
		if ( commandToExecute[0] == 'ssh' ) {
			log.debug("Escaping special characters because of ssh...")
			def temp = []
			for ( String token : commandToExecute ) {
				if ( ! token ) {
					continue
				}
				String tokenTemp = token.replace('"', '\\"')
				//					tokenTemp = tokenTemp.replace('{', '\\{')
				//					tokenTemp = tokenTemp.replace('}', '\\}')
				if ( token != tokenTemp ) {
					tokenTemp = '"'+tokenTemp+'"'
				} else {
					tokenTemp = tokenTemp.replace(' ', '\\ ')
				}
				log.debug("\t\tnew token: "+tokenTemp)

				temp.add(tokenTemp)
			}
			commandToExecute = temp
		}


		log.debug('\n\n'+Joiner.on('\n').join(commandToExecute.iterator())+'\n\n')
		ProcessBuilder procBuilder = new ProcessBuilder(commandToExecute)
		proc = procBuilder.start()

		proc.waitFor()
		ec.setFinished(new Date())

		ec.setExitCode(proc.exitValue())
		def stdout = []
		proc.in.text.split('\n').each { it ->
			def temp = it.trim()
			if ( temp ) {
				stdout.add(it.trim())
			}
		}
		def stderr = []
		proc.err.text.split('\n').each { it ->
			def temp = it.trim()
			if ( temp ) {
				stderr.add(temp)
			}
			ec.setStdOut(stdout)
			ec.setStdErr(stderr)
		}

		if ( Config.debugEnabled() ) {
			log.debug("STDOUT:\n\n"+Joiner.on('\n').join(stdout.iterator()))
			log.debug("\nSTDERR:\n\n"+Joiner.on('\n').join(stderr.iterator()))
		}

		log.debug("Executed advanced command: "+ec.toString())

	}

	private static ExternalCommand executeGoldCommand(List<String> command) {
		ExternalCommand gc = new ExternalCommand(command)
		execute(gc)
		gc.verify()
		return gc
	}

	private static ExternalCommand executeGoldCommand(String command) {
		def list = command.tokenize()
		return executeGoldCommand(list)
	}

	public static List<Machine> getAllMachines() {

		ExternalCommand ec = executeGoldCommand("glsmachine --raw")

		def map = parseGLSOutput(ec.getStdOut())

		def machines = []

		map.each { name, properties ->
			Machine m = getMachine(properties)
			machines.add(m)
		}

		return machines


	}

	public static List<Organization> getAllOrganizations() {

		ExternalCommand ec = executeGoldCommand("goldsh Organization Query --raw")

		def map = parseGLSOutput(ec.getStdOut())

		def orgs = []

		map.each { name, properties ->
			Organization o = getOrganization(properties)
			orgs.add(o)
		}

		return orgs

	}

	public static List<Project> getAllProjects() {

		ExternalCommand ec = executeGoldCommand("glsproject --raw")

		def map = parseGLSOutput(ec.getStdOut())

		def projects = []

		map.each { name, properties ->
			Project p = getProject(properties)
			projects.add(p)
		}

		return projects


	}

	public static List<User> getAllUsers() {
		ExternalCommand ec = executeGoldCommand("glsuser --show Name,CommonName,PhoneNumber,EmailAddress,Organization,Description --raw")

		def map = parseGLSOutput(ec.getStdOut())

		def users = []

		map.each { name, properties ->
			User u = getUser(properties)
			users.add(u)
		}

		return users
	}

	public static Machine getMachine(Map properties) {

		def name = properties.get(NAME_KEY)

		if ( ! name ) {
			throw new MachineFault("Can't get Machine.", "No machine name.", 500)
		}

		def desc = properties.get(DESCRIPTION_KEY)
		def os = properties.get(OPERATING_SYSTEM_KEY)
		def arch = properties.get(ARCHITECTURE_KEY)

		Machine m = new Machine()
		m.setName(name)
		if ( desc ) {
			m.setDescription(desc)
		}
		if (os) {
			m.setOpsys(os);
		}
		if (arch) {
			m.setArch(arch)
		}

		return m

	}

	public static Machine getMachine(String name) {
		ExternalCommand ec = executeGoldCommand("glsmachine --raw " + name)

		def map = parseGLSOutput(ec.getStdOut())

		if (! map[name] ) {
			throw new MachineFault("Can't get machine.", "Machine "+name+" not in gold database", 404)
		}

		return getMachine(map[name])
	}

	public static Organization getOrganization(Map properties) {

		def name = properties.get(NAME_KEY)

		if ( ! name ) {
			throw new OrganizationFault("Can't get Organization.", "No organization name.", 500)
		}

		def desc = properties.get(DESCRIPTION_KEY)

		Organization o = new Organization()
		o.setName(name)

		if ( desc ) {
			o.setDescription(desc)
		}

		return o
	}

	public static Organization getOrganization(String name) {

		ExternalCommand ec = executeGoldCommand("goldsh Organization Query Name==" + name + " --raw")

		def map = parseGLSOutput(ec.getStdOut())

		if (! map[name] ) {
			throw new OrganizationFault("Can't get organization.", "Organization "+name+" not in gold database", 404)
		}

		return getOrganization(map[name])

	}

	public static Project getProject(Map properties) {

		def name = properties.get(NAME_KEY)

		if ( ! name ) {
			throw new ProjectFault("Can't get Project.", "No projectId.", 500)
		}

		def desc = properties.get(DESCRIPTION_KEY)
		def users = properties.get(USERS_KEY)
		def machines = properties.get(MACHINES_KEY)

		Project p = new Project()
		p.setProjectId(name)
		if ( desc ) {
			p.setDescription(desc)
		}

		if ( users ) {
			p.setUsers(users.tokenize(','))
		}

		if ( machines ) {
			p.setMachines(machines.tokenize(','))
		}

		return p

	}

	public static Project getProject(String projectId) {
		ExternalCommand ec = executeGoldCommand("glsproject --raw " + projectId)

		def map = parseGLSOutput(ec.getStdOut())

		if (! map[projectId] ) {
			throw new ProjectFault("Can't get project.", "Project "+projectId+" not in gold database", 404)
		}

		return getProject(map[projectId])
	}

	public static User getUser(Map properties) {

		def name = properties.get(NAME_KEY)

		if ( ! name ) {
			throw new UserFault("Can't create user.", "No username.", 500)
		}

		def phone = properties.get(PHONE_KEY)
		def email = properties.get(EMAIL_KEY)
		def fullName = properties.get(FULLNAME_KEY)
		def org = properties.get(ORGANIZATION_KEY)
		def affiliation = properties.get(AFFILIATION_KEY)

		User u = new User()

		if ( name ) {
			u.setUserId(name)
		}
		if (phone) {
			u.setPhone(phone)
		}
		if (email) {
			u.setEmail(email)
		}
		if (fullName) {
			u.setFullName(fullName)
		}
		if (org) {
			u.setOrganization(org)
		}
		if (affiliation) {
			u.setAffiliation(affiliation)
		}

		return u
	}

	public static User getUser(String username) {

		ExternalCommand ec = executeGoldCommand("glsuser --show Name,CommonName,PhoneNumber,EmailAddress,Organization,Description --raw " + username)

		def map = parseGLSOutput(ec.getStdOut())

		if (! map[username] ) {
			throw new UserFault("Can't get user.", "User "+username+" not in gold database", 404)
		}

		return getUser(map[username])

	}

	static boolean isRegistered(String username) {

		ExternalCommand gc = executeGoldCommand("glsuser -show Name -quiet")

		if (gc.getStdOut().contains(username)) {
			return true
		} else {
			return false
		}
	}


	public static boolean machineExists(String id) {

		for (Machine m : getAllMachines() ) {
			if ( id == m.getName() ) {
				return true;
			}
		}

		return false;

	}


	static void main(def args){

		Project p = getProject("testproject5")

		println p.getUsers()
	}

	public static void modifyMachine(String name, String arch, String os, String desc) {

		if (!name) {
			throw new MachineFault("Can't modify machine.", "No machine name specified.", 400);
		}

		if ( ! machineExists(name) ) {
			throw new MachineFault("Can't modify machine.", "Machine "+name+" does not exist in Gold", 404);
		}

		List<String> command = Lists.newArrayList("gchmachine")
		if (StringUtils.isNotBlank(arch)) {
			command.add('--arch')
			command.add(arch)
		}
		if (StringUtils.isNotBlank(os)) {
			command.add('--opsys')
			command.add(os)
		}
		if (StringUtils.isNotBlank(desc)) {
			command.add('-d')
			command.add(desc)
		}

		command.add(name)

		ExternalCommand ec = executeGoldCommand(command)

	}

	public static void modifyProject(String projectId, List<String> machines, String clazz, String description) {

		if (StringUtils.isBlank(projectId)) {
			throw new ProjectFault("Can't modify project.",
			"projectId not specified.", 500)
		}

		if (!projectExists(projectId)) {
			throw new ProjectFault("Can't modify project.", "project " + projectId
			+ " not in Gold database.", 404)
		}


		List<String> command = Lists.newArrayList("gchproject")
		if (StringUtils.isNotBlank(description)) {
			command.add("-d")
			command.add(description)
		}

		if ( machines ) {
			for ( String m : machines ) {
				if (! machineExists(m)) {
					throw new ProjectFault("Can't modify project "+projectId, "Machine "+m+" does not exist in Gold.", 400)
				}
			}
			command.add("--addMachines")
			command.add(StringUtils.join(machines, ','))
		}

		if ( clazz ) {
			command.add("--extension")
			command.add("Class="+clazz)

			command.add("--extension")
			if ( "ResearchFunded" == clazz ) {
				command.add("Funded=True")
			} else {
				command.add("Funded=False")
			}
		}

		command.add(projectId)

		ExternalCommand ec = executeGoldCommand(command)


	}

	public static void modifyUser(String username, String organization, String affiliation, String fullName, String email, String phone) {

		if (StringUtils.isBlank(username)) {
			throw new UserFault("Can't modify user.",
			"Username not specified.")
		}

		if (!isRegistered(username)) {
			throw new UserFault("Can't modify user.", "User " + username
			+ " not in Gold database.", 404)
		}


		List<String> command = Lists.newArrayList("gchuser")
		if (StringUtils.isNotBlank(fullName)) {
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

		if (StringUtils.isNotBlank(affiliation)) {
			command.add("--extension")
			command.add("Affiliation="+affiliation)
		}

		if (StringUtils.isNotBlank(organization)) {
			command.add("--extension")
			command.add("Organization="+organization)
		}

		command.add(username)

		ExternalCommand ec = executeGoldCommand(command)


	}

	public static boolean organizationExists(String name) {
		for (Organization o : getAllOrganizations()) {
			if ( name == o.getName()) {
				return true;
			}
		}
		return false;
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

	public static boolean projectExists(String id) {

		for (Project p : getAllProjects() ) {
			if ( id == p.getProjectId() ) {
				return true;
			}
		}

		return false;

	}


}
