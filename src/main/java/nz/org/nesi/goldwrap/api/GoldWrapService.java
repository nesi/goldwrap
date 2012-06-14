package nz.org.nesi.goldwrap.api;

import java.util.List;

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import nz.org.nesi.goldwrap.domain.Machine;
import nz.org.nesi.goldwrap.domain.Project;
import nz.org.nesi.goldwrap.domain.User;
import nz.org.nesi.goldwrap.errors.UserFault;

/**
 * This service API publishes methods to interact with a Gold instance.
 * 
 * @author markus
 * 
 */
@WebService(serviceName = "GoldWrapService")
public interface GoldWrapService {

	/**
	 * Creates a {@link Project} in the Gold database.
	 * 
	 * You can specify a list of {@link User} objects within the project
	 * description. Every user needs to have at least the
	 * {@link User#getUserId()} property set, otherwise this method will fail.
	 * If the user already exists it will be updated with (optional) additional
	 * fields within the user object, if not, the user will be created in Gold.
	 * 
	 * In addition, an account will be automatically created in Gold (account
	 * name: project name prefixed with acc_) and linked to this project and the
	 * users of this project.
	 * 
	 * @param projName
	 *            the name of the new project
	 * @param users
	 *            a list of users
	 * @param properties
	 *            a list of properties of this project
	 */
	@POST
	@Path("/projects")
	@Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public void createProject(Project proj);

	/**
	 * Gets all projects from the Gold database.
	 * 
	 * @return a list of all {@link Project}s
	 */
	@GET
	@Path("/projects")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public List<Project> getProjects();

	/**
	 * Returns information about the project with the specified project name
	 * 
	 * @param projectName
	 *            the project name
	 * @return the Project object
	 */
	@GET
	@Path("/projects/{projectName}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public Project getProject(@PathParam("projectName") String projectName);

	/**
	 * Modifies the description field within Gold for this project.
	 * 
	 * Note, it's not possible to remove users from a project using this method,
	 * users specified here will be added, no current user will be removed.
	 * 
	 * @param projName
	 *            the name of the project
	 * @param project
	 *            the new project description
	 * @return the updated project
	 */
	@POST
	@Path("/projects/{projectName}")
	@Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public Project modifyProject(@PathParam("projectName") String projName,
			Project project);

	/**
	 * Deletes a project.
	 * 
	 * @param projName
	 *            the project name.
	 */
	@DELETE
	@Path("/projects/{projectName}")
	public void deleteProject(@PathParam("projectName") String projName);

	/**
	 * Returns all users that are members of this project.
	 * 
	 * @param projName
	 *            the name of the project
	 * @return the users
	 */
	@GET
	@Path("/projects/{projectName}/users")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public List<User> getUsersForProject(
			@PathParam("projectName") String projName);

	/**
	 * Adds a user to this project.
	 * 
	 * The user will also be added to the account that is associated with this
	 * project.
	 * 
	 * @param projName
	 *            the name of the project
	 * @param userId
	 *            the id of the user to add ('text/plain')
	 * @return the updated project details
	 */
	@POST
	@Path("/projects/{projectName}/add_user")
	@Consumes("text/plain")
	public Project addUserToProject(@PathParam("projectName") String projName,
			String userId);

	/**
	 * Returns a list of all users currently registered in Gold.
	 * 
	 * @return a list of all {@link User}s
	 */
	@GET
	@Path("/users")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public List<User> getUsers();

	/**
	 * Creates a {@link User} in the Gold database.
	 * 
	 * @param user
	 *            the {@link User} object
	 * @throws UserFault
	 *             if the {@link User} object does not have a valid username
	 *             specified or the username already exists in Gold
	 */
	@POST
	@Path("/users")
	@Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public void createUser(User user);

	/**
	 * Returns registration details about this user by querying Gold.
	 * 
	 * @param username
	 *            the username
	 * @return the {@link User} object
	 */
	@GET
	@Path("/users/{username}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public User getUser(@PathParam("username") String username);

	/**
	 * Changes user details.
	 * 
	 * @param username
	 *            the username
	 * @param user
	 *            the new user details
	 */
	@POST
	@Path("/users/{username}")
	@Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public void modifyUser(@PathParam("username") String username, User user);

	/**
	 * Deletes this user from the Gold database.
	 * 
	 * @param username
	 *            the username
	 */
	@DELETE
	@Path("/users/{username}")
	public void deleteUser(@PathParam("username") String username);

	/**
	 * Queries and returns all projects for this user.
	 * 
	 * @param username
	 *            the userId
	 * @return a list of all {@link Project}s
	 */
	@GET
	@Path("/users/{username}/projects")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public List<Project> getProjectsForUser(
			@PathParam("username") String username);

	/**
	 * Gets a list of all the machines that are configured in Gold.
	 * 
	 * @return a list of all {@link Machine}s
	 */
	@GET
	@Path("/machines")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public List<Machine> getMachines();

	/**
	 * Creates a machine resource in Gold.
	 * 
	 * @param mach
	 *            the machine to create
	 */
	@POST
	@Path("/machines")
	@Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public void createMachine(Machine mach);

	/**
	 * Gets details about the machine in question from Gold.
	 * 
	 * @param machineName
	 *            the name of the machine
	 * @return the machine details
	 */
	@GET
	@Path("/machines/{machineName}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public Machine getMachine(@PathParam("machineName") String machineName);

	/**
	 * Modifies an existing Machine within Gold.
	 * 
	 * @param machName
	 *            the name of the machine
	 * @param machine
	 *            the new details of this machine. Empty values in this will not
	 *            delete existing values in Gold database.
	 * @return the new machine object as it is stored in Gold
	 */
	@POST
	@Path("/machines/{machineName}")
	@Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public Machine modifyMachine(@PathParam("machineName") String machName,
			Machine machine);

}
