package nz.org.nesi.goldwrap.impl;

import java.util.List;

import javax.jws.WebService;
import javax.ws.rs.Path;

import nz.org.nesi.goldwrap.api.GoldWrapService;
import nz.org.nesi.goldwrap.domain.Project;
import nz.org.nesi.goldwrap.domain.User;
import nz.org.nesi.goldwrap.errors.GoldCommandFault;
import nz.org.nesi.goldwrap.util.GoldWrap;

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

	public void createUser(User user) {

		String username = user.getUserId();
		String fullname = user.getFullName();
		String email = user.getEmail();
		String phone = user.getPhone();

		GoldWrap.createUser(username, fullname, email, phone);
	}

	public void deleteUser(String username) {

		GoldWrap.deleteUser(username);

	}

	public List<User> getAllUsers() {
		return GoldWrap.getAllUsers();
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

	public void modifyUser(User user) throws GoldCommandFault {

		String username = user.getUserId();
		String fullname = user.getFullName();
		String email = user.getEmail();
		String phone = user.getPhone();

		GoldWrap.modifyUser(username, fullname, email, phone);

	}

	public List<Project> getAllProjects() {
		return GoldWrap.getAllProjects();
	}

	public Project getProject(String projectId) {
		return GoldWrap.getProject(projectId);
	}

	public void createProject(Project project) {
		String id = project.getProjectId();
		String desc = project.getDescription();
		GoldWrap.createProject(id, desc);
	}

	public void deleteProject(String id) {
		GoldWrap.deleteProject(id);
	}

	public void modifyProject(Project proj) {

		String id = proj.getProjectId();
		String desc = proj.getDescription();

		GoldWrap.modifyProject(id, desc);

	}

	public void addUserToProject(String projectId, String username) {

		GoldWrap.addUserToProject(projectId, username);

	}
}
