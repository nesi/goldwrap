package nz.org.nesi.goldwrap.util


import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils


import groovy.util.logging.Slf4j

import com.google.common.base.Splitter
import com.google.common.collect.Lists

import nz.org.nesi.goldwrap.domain.ExternalCommand
import nz.org.nesi.goldwrap.domain.Machine
import nz.org.nesi.goldwrap.domain.Project
import nz.org.nesi.goldwrap.domain.User
import nz.org.nesi.goldwrap.errors.MachineFault
import nz.org.nesi.goldwrap.errors.ProjectFault;
import nz.org.nesi.goldwrap.errors.ServiceException;
import nz.org.nesi.goldwrap.errors.UserFault
import nz.org.nesi.goldwrap.utils.JSONHelpers

@Slf4j
class GoldHelper {

	private static ExternalCommand executeGoldCommand(String command) {
		ExternalCommand gc = new ExternalCommand(command);
		gc.execute();
		gc.verify();
		return gc;
	}

	static final String NAME_KEY = "Name"
	static final String DESCRIPTION_KEY = "Description"
	static final String USERS = "Users"
	static final String ACTIVE_KEY = "Active"
	static final String ARCHITECTURE_KEY = "Architecture"
	static final String OPERATING_SYSTEM_KEY = "OperatingSystem"
	static final String ORGANIZATION_KEY = "Organization"

	static void main(def args) {

		ExternalCommand ec = new ExternalCommand('glsuser -A --raw')
		ec.execute()
		println getAllUsers()
	}

	static def parseGLSOutput(def output) {

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
			def name = map.get(NAME_KEY)
			if ( name ) {
				result[map.get(NAME_KEY)] = map
			}
		}
		result
	}

	static Project addUserToProject(String projName, String user) {

		if ( ! isRegistered(user) ) {
			throw new UserFault("Can't retrieve user.", "User " + user
			+ " not in Gold database.", 404);
		}

		ExternalCommand ec = new ExternalCommand('gchproject --addUser '+user+ " "+projName)
		ec.execute()

		Project p = getProject(projName)
		if (! p.getUsers().contains(user)) {
			throw new ProjectFault(p, "Could not add user "+user+" to project "+projName, "Unknown reason.", 500);
		}
		p
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

	static boolean isRegistered(String username) {

		ExternalCommand gc = executeGoldCommand("glsuser -show Name -quiet");

		if (gc.getStdOut().contains(username)) {
			return true;
		} else {
			return false;
		}
	}

	static boolean projectExists(String projName) {

		ExternalCommand gc = executeGoldCommand("glsproject -show Name -quiet");

		if (gc.getStdOut().contains(projName)) {
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
			throw new ProjectFault("Can't create project "+key+".", "Can't read description field for project "+key, 500)
		}

		def users = value[USERS].split (',') as List
		proj.setUsers(users)


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

	public static List<User> getUsersForProject(String projName) {

		def proj = getProject(projName)

		def result = []
		proj.getUsers().each { it ->
			try {
				User u = getUser(it)
				System.out.println("USER: "+u)
				result.add(u)
			}  catch (all) {
				UserFault f = new UserFault("Can't load user '"+it+"'.", "Error retrieving user '"+it+"' from Gold.", 500)
				f.getFaultInfo().setException(ExceptionUtils.getStackTrace(all))
				throw f
			}
		}

		result
	}
}
