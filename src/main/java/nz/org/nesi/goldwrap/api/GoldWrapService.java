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
	 * Gets all projects from the Gold database.
	 */
	@GET
	@Path("/projects")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	public List<Project> getProjects();

	/**
	 * Creates a {@link Project} in the Gold database
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
	 * @return all users
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

}
