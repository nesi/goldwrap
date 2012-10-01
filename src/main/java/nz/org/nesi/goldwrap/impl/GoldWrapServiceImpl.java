package nz.org.nesi.goldwrap.impl;

import java.util.List;

import javax.jws.WebService;
import javax.ws.rs.Path;

import nz.org.nesi.goldwrap.api.GoldWrapService;
import nz.org.nesi.goldwrap.domain.Machine;
import nz.org.nesi.goldwrap.domain.Organization;
import nz.org.nesi.goldwrap.domain.Project;
import nz.org.nesi.goldwrap.domain.User;
import nz.org.nesi.goldwrap.errors.GoldCommandFault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService(endpointInterface = "nz.org.nesi.goldwrap.api.GoldWrapService", name = "GoldWrapService")
@Path("/goldwrap")
public class GoldWrapServiceImpl implements GoldWrapService {

	public static Logger myLogger = LoggerFactory
			.getLogger(GoldWrapServiceImpl.class);

	private static volatile boolean initialized = false;

	public GoldWrapServiceImpl() {
		initialize();
	}

	public void addUserToProject(String projectId, String username) {

		GoldWrap.addUserToProject(projectId, username);

	}

	public void createMachine(Machine machine) {

		String name = machine.getName();
		String os = machine.getOpsys();
		String desc = machine.getDescription();
		String arch = machine.getArch();

		GoldWrap.createMachine(name, arch, os, desc);

	}

	public void createOrganization(Organization org) {

		String name = org.getName();
		String desc = org.getDescription();

		GoldWrap.createOrganization(name, desc);
	}

	public void createProject(Project project) {
		String id = project.getProjectId();
		String desc = project.getDescription();
		GoldWrap.createProject(id, desc);
	}

	public void createUser(User user) {

		String username = user.getUserId();
		String fullname = user.getFullName();
		String email = user.getEmail();
		String phone = user.getPhone();
		String org = user.getOrganization();

		GoldWrap.createUser(username, fullname, org, email, phone);
	}

	public void deleteProject(String id) {
		GoldWrap.deleteProject(id);
	}

	public void deleteUser(String username) {

		GoldWrap.deleteUser(username);

	}

	public List<Machine> getAllMachines() {
		return GoldWrap.getAllMachines();
	}

	public List<Organization> getAllOrgainzations() {
		return GoldWrap.getAllOrganizations();
	}

	public List<Project> getAllProjects() {
		return GoldWrap.getAllProjects();
	}

	public List<User> getAllUsers() {
		return GoldWrap.getAllUsers();
	}

	public Machine getMachine(String machine_name) {
		return GoldWrap.getMachine(machine_name);
	}

	public Organization getOrganization(String name) {
		return GoldWrap.getOrganization(name);
	}

	public Project getProject(String projectId) {
		return GoldWrap.getProject(projectId);
	}

	public User getUser(String username) {

		return GoldWrap.getUser(username);

	}

	public synchronized void initialize() {

		if (!initialized) {
			try {
				myLogger.debug("Initializing...");
			} finally {
				initialized = true;
			}
		}

	}

	public void modifyMachine(Machine machine) {

		String name = machine.getName();
		String os = machine.getOpsys();
		String desc = machine.getDescription();
		String arch = machine.getArch();

		GoldWrap.modifyMachine(name, arch, os, desc);
	}

	public void modifyProject(Project proj) {

		String id = proj.getProjectId();
		String desc = proj.getDescription();

		GoldWrap.modifyProject(id, desc);

	}

	public void modifyUser(User user) throws GoldCommandFault {

		String username = user.getUserId();
		String fullname = user.getFullName();
		String email = user.getEmail();
		String phone = user.getPhone();

		GoldWrap.modifyUser(username, fullname, email, phone);

	}
}
