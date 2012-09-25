package nz.org.nesi.goldwrap.util

import groovy.util.logging.Slf4j
import nz.org.nesi.goldwrap.Config
import nz.org.nesi.goldwrap.domain.ExternalCommand
import nz.org.nesi.goldwrap.domain.Project
import nz.org.nesi.goldwrap.domain.User
import nz.org.nesi.goldwrap.errors.ProjectFault
import nz.org.nesi.goldwrap.errors.UserFault

import org.apache.commons.lang3.StringUtils

import com.google.common.base.Joiner
import com.google.common.base.Splitter
import com.google.common.collect.Lists

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


	public static void createProject(String projectId, String description) {

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

		command.add(projectId)

		ExternalCommand ec = executeGoldCommand(command)

		if (!GoldWrap.projectExists(projectId)) {
			throw new ProjectFault("Can't create project "+projectId+".", "Unknown reason", 500)
		}
	}

	public static void createUser(String username, String fullName, String email, String phone) {

		if ( StringUtils.isBlank(username) ) {
			throw new UserFault("Can't create user.", "Username not specified", 400)
		}

		if ( StringUtils.isBlank(fullName) ) {
			throw new UserFault("Can't create user.", "Full name not specified", 400)
		}

		if ( StringUtils.isBlank(email) ) {
			throw new UserFault("Can't create user.", "Email address specified", 400)
		}


		if (isRegistered(username)) {
			throw new UserFault("Can't create user.", "User " + username
			+ " already in Gold database.", 409)
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
		ExternalCommand ec = executeGoldCommand("glsuser --raw")

		def map = parseGLSOutput(ec.getStdOut())

		def users = []

		map.each { name, properties ->
			User u = getUser(properties)
			users.add(u)
		}

		return users
	}

	public static Project getProject(Map properties) {

		def name = properties.get(NAME_KEY)

		if ( ! name ) {
			throw new ProjectFault("Can't create Project.", "No projectId.", 500)
		}

		def desc = properties.get(DESCRIPTION_KEY)
		def users = properties.get(USERS_KEY)

		Project p = new Project()
		p.setProjectId(name)
		if ( desc ) {
			p.setDescription(desc)
		}

		if ( users ) {
			p.setUsers(users.tokenize(','))
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

		return u
	}

	public static User getUser(String username) {

		ExternalCommand ec = executeGoldCommand("glsuser --raw " + username)

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

	static void main(def args){

		Project p = getProject("testproject5")

		println p.getUsers()
	}











	public static void modifyUser(String username, String fullName, String email, String phone) {

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

		command.add(username)

		ExternalCommand ec = executeGoldCommand(command)


	}

	public static void modifyProject(String projectId, String description) {

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

		command.add(projectId)

		ExternalCommand ec = executeGoldCommand(command)


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
