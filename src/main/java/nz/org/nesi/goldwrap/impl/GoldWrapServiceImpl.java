package nz.org.nesi.goldwrap.impl;

import java.util.List;

import javax.jws.WebService;
import javax.ws.rs.Path;

import nz.org.nesi.goldwrap.api.GoldWrapService;
import nz.org.nesi.goldwrap.domain.ExternalCommand;
import nz.org.nesi.goldwrap.domain.Project;
import nz.org.nesi.goldwrap.domain.User;
import nz.org.nesi.goldwrap.errors.ProjectFault;
import nz.org.nesi.goldwrap.errors.ServiceException;
import nz.org.nesi.goldwrap.errors.UserFault;
import nz.org.nesi.goldwrap.util.GoldHelper;
import nz.org.nesi.goldwrap.utils.JSONHelpers;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;

@WebService(endpointInterface = "nz.org.nesi.goldwrap.api.GoldWrapService", name = "GoldWrapService")
@Path("/goldwrap")
public class GoldWrapServiceImpl implements GoldWrapService {

	private static ExternalCommand executeGoldCommand(String command) {
		ExternalCommand gc = new ExternalCommand(command);
		gc.execute();
		gc.verify();
		return gc;
	}

	private void checkProjectname(String projectname) {
		if (StringUtils.isBlank(projectname)) {
			throw new ServiceException("Can't execute operation.",
					"Projectname blank or not specified.");
		}
	}

	private void checkUsername(String username) {
		if (StringUtils.isBlank(username)) {
			throw new ServiceException("Can't execute operation.",
					"Username blank or not specified.");
		}
	}

	public void createProject(Project proj) {

		String projName = proj.getProjectId();

		proj.validate(true);

		if (GoldHelper.projectExists(projName)) {
			throw new ProjectFault(proj, "Can't create project " + projName,
					"Project name '" + projName + "' already exists in Gold.");
		}

		for (String userId : proj.getUsers()) {
			if (!GoldHelper.isRegistered(userId)) {
				throw new ProjectFault(proj,
						"Can't create project " + projName, "User '" + userId
								+ "' does not exist in Gold yet.");
			}
		}

		StringBuffer command = new StringBuffer("gmkproject ");

		String desc = JSONHelpers.convertToJSONString(proj);
		command.append("-d '" + desc + "' ");

		String users = Joiner.on(",").join(proj.getUsers());

		if (StringUtils.isNotBlank(users)) {
			command.append("-u '" + users + "' ");
		}

		command.append(projName);

		ExternalCommand ec = executeGoldCommand(command.toString());

		if (!GoldHelper.projectExists(projName)) {
			throw new ProjectFault(proj, "Can't create project.",
					"Unknow reason");
		}

	}

	public void createUser(User user) {

		user.validate();

		String username = user.getUserId();
		String phone = user.getPhone();
		String email = user.getAltEmail();
		String fullName = user.getLastName() + ", " + user.getFirstName();
		String institution = user.getInstitution();

		if (GoldHelper.isRegistered(username)) {
			throw new UserFault("Can't create user.", "User " + username
					+ " already in Gold database.", 409);
		}

		String desc = JSONHelpers.convertToJSONString(user);

		String command = null;
		if (StringUtils.isBlank(email)) {
			command = "gmkuser -n \"" + fullName + "\" -d '" + desc + "' -F "
					+ phone + " " + username;
		} else {
			command = "gmkuser -n \"" + fullName + "\" -E " + email + " -d '"
					+ desc + "' -F " + phone + " " + username;
		}

		ExternalCommand ec = executeGoldCommand(command);

		if (!GoldHelper.isRegistered(username)) {
			throw new UserFault(user, "Can't create user.", "Unknown reason");
		}

	}

	public void deleteProject(String projName) {

		checkProjectname(projName);

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

	public Project getProject(String projName) {

		checkProjectname(projName);

		return GoldHelper.getProject(projName);

	}

	public List<Project> getProjects() {
		return GoldHelper.getAllProjects();
	}

	public List<Project> getProjectsForUser(String username) {
		return GoldHelper.getProjectsForUser(username);
	}

	public User getUser(String username) {

		checkUsername(username);

		User u = GoldHelper.getUser(username);
		return u;

	}

	public List<User> getUsers() {
		return GoldHelper.getAllUsers();
	}

	public List<User> getUsersForProject(String projName) {
		return GoldHelper.getUsersForProject(projName);
	}

	public boolean isRegistered(String user) {
		return GoldHelper.isRegistered(user);
	}

	public Project modifyProject(String projName, Project project) {

		checkProjectname(projName);

		if (StringUtils.isNotBlank(project.getProjectId())
				&& !projName.equals(project.getProjectId())) {
			throw new ProjectFault(project, "Can't modify project.",
					"Project name can't be changed.");
		}

		if (!GoldHelper.projectExists(projName)) {
			throw new ProjectFault("Can't modify project.", "Project "
					+ projName + " not in Gold database.", 404);
		}

		for (String userId : project.getUsers()) {
			if (!GoldHelper.isRegistered(userId)) {
				throw new ProjectFault(project, "Can't modify project "
						+ projName, "User '" + userId
						+ "' does not exist in Gold yet.");
			}
		}

		project.validate(false);

		StringBuffer command = new StringBuffer("gchproject ");
		String desc = JSONHelpers.convertToJSONString(project);
		command.append("-d '" + desc + "' ");

		String users = Joiner.on(",").join(project.getUsers());

		if (StringUtils.isNotBlank(users)) {
			command.append("--addUsers '" + users + "' ");
		}

		command.append(projName);

		ExternalCommand ec = executeGoldCommand(command.toString());

		return getProject(projName);

	}

	public void modifyUser(String username, User user) {

		if (StringUtils.isBlank(username)) {
			throw new UserFault(user, "Can't modify user.",
					"Username field can't be blank.");
		}

		if (StringUtils.isNotBlank(user.getUserId())
				&& !username.equals(user.getUserId())) {
			throw new UserFault(user, "Can't modify user.",
					"Username can't be changed.");
		}

		if (!GoldHelper.isRegistered(username)) {
			throw new UserFault("Can't modify user.", "User " + username
					+ " not in Gold database.", 404);
		}

		user.validate();

		String fullName = user.getLastName() + ", " + user.getFirstName();
		String phone = user.getPhone();
		String institution = user.getInstitution();
		String email = user.getAltEmail();

		String desc = JSONHelpers.convertToJSONString(user);

		String command = null;
		if (StringUtils.isBlank(email)) {
			command = "gchuser -n \"" + fullName + "\" -d '" + desc + "' -F "
					+ phone + " " + username;
		} else {
			command = "gchuser -n \"" + fullName + "\" -E " + email + " -d '"
					+ desc + "' -F " + phone + " " + username;

		}

		ExternalCommand ec = executeGoldCommand(command);

		if (!GoldHelper.isRegistered(username)) {
			throw new UserFault(user, "Can't create user.", "Unknown reason");
		}

	}

	public Project addUserToProject(String projName, String userId) {
		return GoldHelper.addUserToProject(projName, userId);
	}
}
