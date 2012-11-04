package nz.org.nesi.goldwrap.api;

import java.util.List;

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import nz.org.nesi.goldwrap.domain.Allocation;
import nz.org.nesi.goldwrap.domain.DepositAllocation;
import nz.org.nesi.goldwrap.domain.Machine;
import nz.org.nesi.goldwrap.domain.Organization;
import nz.org.nesi.goldwrap.domain.Project;
import nz.org.nesi.goldwrap.domain.User;
import nz.org.nesi.goldwrap.errors.GoldCommandFault;

/**
 * This service API publishes methods to interact with a Gold instance.
 * 
 * @author markus
 * 
 */
@WebService(serviceName = "GoldWrapService")
public interface GoldWrapService {

	/**
	 * Adds an allocation to a project.
	 * 
	 * @param projectId
	 *            the projectId
	 * @param alloc
	 *            the allocation
	 */
	@POST
	@Path("/projects/{projectId}/deposit")
	public void addAllocation(@PathParam("projectId") String projectId,
			DepositAllocation alloc);

	/**
	 * Delete the allocation with the given id.
	 * 
	 * @param id
	 *            allocation id
	 */
	@DELETE
	@Path("/allocations/{allocationId}")
	public void deleteAllocation(@PathParam("allocationId") int id);

	/**
	 * Modifies allocations.
	 * 
	 * Be ware that only the start time, end time and credit limit as well as
	 * the description can be changed.
	 * 
	 * @param alloc
	 *            the allocation
	 */
	@POST
	@Path("/allocations")
	public void modifyAllocation(Allocation alloc);

	/**
	 * Get the allocation with the given id.
	 * 
	 * @param id
	 *            allocation id
	 * @return
	 */
	@GET
	@Path("/allocations/{allocationId}")
	public Allocation getAllocation(@PathParam("allocationId") int id);

	/**
	 * Queries Gold for all allocations that were made for this project.
	 * 
	 * @param projectId
	 *            the name of the project
	 */
	@GET
	@Path("/projects/{projectId}/allocations")
	public List<Allocation> getAllocations(
			@PathParam("projectId") String projectId);

	/**
	 * Creates a user in Gold.
	 * 
	 * @param username
	 *            the username in Gold (Tuakiri hash)
	 * @param fullname
	 *            the full name of the User
	 * @param email
	 *            the email address of the user
	 * @param phone
	 *            the phone number of the user
	 */
	@PUT
	@Path("/users")
	public void createUser(User user);

	/**
	 * Modifies this user
	 * 
	 * @param user
	 * @throws GoldCommandFault
	 */
	@POST
	@Path("/users")
	public void modifyUser(User user);

	/**
	 * Deletes the user with the specified id.
	 * 
	 * @param userId
	 *            the userid
	 */
	@DELETE
	@Path("/users/{username}")
	public void deleteUser(@PathParam("username") String userId);

	/**
	 * Retrives the user with the specified username.
	 * 
	 * @param username
	 *            the username
	 * @return the user object
	 */
	@GET
	@Path("/users/{username}")
	public User getUser(@PathParam("username") String username);

	/**
	 * Gets a list of all users that are currently registered in Gold.
	 * 
	 * @return the list of all users
	 */
	@GET
	@Path("/users")
	public List<User> getAllUsers();

	/**
	 * Gets a list of all projects in Gold.
	 * 
	 * @return the projects
	 */
	@GET
	@Path("/projects")
	public List<Project> getAllProjects();

	/**
	 * Gets the project with the specified id from Gold.
	 * 
	 * @param projectId
	 *            the projectId
	 * @return the project
	 */
	@GET
	@Path("/projects/{projectId}")
	public Project getProject(@PathParam("projectId") String projectId);

	/**
	 * Creates a project in Gold.
	 * 
	 * @param p
	 *            the project
	 */
	@PUT
	@Path("/projects")
	public void createProject(Project p);

	/**
	 * Deletes the project.
	 * 
	 * @param id
	 *            the projectid
	 */
	@DELETE
	@Path("/projects/{projectId}")
	public void deleteProject(@PathParam("projectId") String id);

	/**
	 * Changes the details of the project.
	 * 
	 * This doesn't add or remove users.
	 * 
	 * @param proj
	 *            the project
	 */
	@POST
	@Path("/projects")
	public void modifyProject(Project proj);

	/**
	 * Adds a user to a project.
	 * 
	 * @param projectId
	 *            the project name
	 * @param username
	 *            the username
	 */
	@POST
	@Path("/projects/{projectId}/add_user")
	@Consumes("text/plain")
	public void addUserToProject(@PathParam("projectId") String projectId,
			String username);

	/**
	 * Creates a machine in Gold.
	 * 
	 * @param machine
	 *            the machine
	 */
	@PUT
	@Path("/machines")
	public void createMachine(Machine machine);

	/**
	 * Returns a list of all machines.
	 * 
	 * @return the machines
	 */
	@GET
	@Path("/machines")
	public List<Machine> getAllMachines();

	/**
	 * Returns the machine with the specified name.
	 * 
	 * @param machine_name
	 *            the machine name
	 * @return the machine
	 */
	@GET
	@Path("/machines/{machine_name}")
	public Machine getMachine(@PathParam("machine_name") String machine_name);

	/**
	 * Modifies a machine.
	 * 
	 * @param machine
	 *            the machine
	 */
	@POST
	@Path("/machines")
	public void modifyMachine(Machine machine);

	/**
	 * Creates a new organization in Gold.
	 * 
	 * @param org
	 *            the organization
	 */
	@PUT
	@Path("/organizations")
	public void createOrganization(Organization org);

	/**
	 * Returns a list of all organizations in the Gold database.
	 * 
	 * @return the organizations
	 */
	@GET
	@Path("/organizations")
	public List<Organization> getAllOrgainzations();

	/**
	 * Retrieve the organization with the specified name.
	 * 
	 * @param orgname
	 *            the name of the organization
	 * @return the organization
	 */
	@GET
	@Path("/organizations/{orgname}")
	public Organization getOrganization(@PathParam("orgname") String orgname);

	// /**
	// * Creates a {@link Project} in the Gold database.
	// *
	// * You can specify a list of {@link User} objects within the project
	// * description. Every user needs to have at least the
	// * {@link User#getUserId()} property set, otherwise this method will fail.
	// * If the user already exists it will be updated with (optional)
	// additional
	// * fields within the user object, if not, the user will be created in
	// Gold.
	// *
	// * In addition, an account will be automatically created in Gold (account
	// * name: project name prefixed with acc_) and linked to this project and
	// the
	// * users of this project.
	// *
	// * @param projName
	// * the name of the new project
	// * @param users
	// * a list of users
	// * @param properties
	// * a list of properties of this project
	// * @return
	// */
	// @POST
	// @Path("/projects")
	// @Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public Project createProject(Project proj);
	//
	// /**
	// * Gets all projects from the Gold database.
	// *
	// * @return a list of all {@link Project}s
	// */
	// @GET
	// @Path("/projects")
	// @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public List<Project> getProjects();
	//
	// /**
	// * Returns information about the project with the specified project name
	// *
	// * @param projectName
	// * the project name
	// * @return the Project object
	// */
	// @GET
	// @Path("/projects/{projectName}")
	// @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public Project getProject(@PathParam("projectName") String projectName);
	//
	// /**
	// * Modifies the description field within Gold for this project.
	// *
	// * Note, it's not possible to remove users from a project using this
	// method,
	// * users specified here will be added, no current user will be removed.
	// *
	// * @param projName
	// * the name of the project
	// * @param project
	// * the new project description
	// * @return the updated project
	// */
	// @POST
	// @Path("/projects/{projectName}")
	// @Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public Project modifyProject(@PathParam("projectName") String projName,
	// Project project);
	//
	// /**
	// * Deletes a project.
	// *
	// * @param projName
	// * the project name.
	// */
	// @DELETE
	// @Path("/projects/{projectName}")
	// public void deleteProject(@PathParam("projectName") String projName);
	//
	// /**
	// * Returns all users that are members of this project.
	// *
	// * @param projName
	// * the name of the project
	// * @return the users
	// */
	// @GET
	// @Path("/projects/{projectName}/users")
	// @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public List<User> getUsersForProject(
	// @PathParam("projectName") String projName);
	//
	// /**
	// * Deposit an allocation into a project.
	// *
	// * @param alloc
	// * the allocation to deposit
	// */
	// @POST
	// @Path("/projects/{projectName}/deposit")
	// @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public void deposit(@PathParam("projectName") String proj, Allocation
	// alloc);
	//
	// /**
	// * Adds a user to this project.
	// *
	// * The user will also be added to the account that is associated with this
	// * project.
	// *
	// * @param projName
	// * the name of the project
	// * @param userId
	// * the id of the user to add ('text/plain')
	// * @return the updated project details
	// */
	// @POST
	// @Path("/projects/{projectName}/add_user")
	// @Consumes("text/plain")
	// public Project addUserToProject(@PathParam("projectName") String
	// projName,
	// String userId);
	//
	// /**
	// * Returns a list of all users currently registered in Gold.
	// *
	// * @return a list of all {@link User}s
	// */
	// @GET
	// @Path("/users")
	// @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public List<User> getUsers();
	//
	// /**
	// * Creates a {@link User} in the Gold database.
	// *
	// * @param user
	// * the {@link User} object
	// * @throws UserFault
	// * if the {@link User} object does not have a valid username
	// * specified or the username already exists in Gold
	// */
	// @POST
	// @Path("/users")
	// @Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public void createUser(User user);
	//
	// /**
	// * Returns registration details about this user by querying Gold.
	// *
	// * @param username
	// * the username
	// * @return the {@link User} object
	// */
	// @GET
	// @Path("/users/{username}")
	// @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public User getUser(@PathParam("username") String username);
	//
	// /**
	// * Changes user details.
	// *
	// * @param username
	// * the username
	// * @param user
	// * the new user details
	// */
	// @POST
	// @Path("/users/{username}")
	// @Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public void modifyUser(@PathParam("username") String username, User
	// user);
	//
	// /**
	// * Deletes this user from the Gold database.
	// *
	// * @param username
	// * the username
	// */
	// @DELETE
	// @Path("/users/{username}")
	// public void deleteUser(@PathParam("username") String username);
	//
	// /**
	// * Queries and returns all projects for this user.
	// *
	// * @param username
	// * the userId
	// * @return a list of all {@link Project}s
	// */
	// @GET
	// @Path("/users/{username}/projects")
	// @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public List<Project> getProjectsForUser(
	// @PathParam("username") String username);
	//
	// /**
	// * Queries and returns all projects where this user is the principal.
	// *
	// * @param username
	// * the userId
	// * @return a list of all {@link Project}s
	// */
	// @GET
	// @Path("/users/{username}/principal")
	// @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public List<Project> getProjectsWhereUserIsPrincipal(
	// @PathParam("username") String username);
	//
	// /**
	// * Gets a list of all the machines that are configured in Gold.
	// *
	// * @return a list of all {@link Machine}s
	// */
	// @GET
	// @Path("/machines")
	// @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public List<Machine> getMachines();
	//
	// /**
	// * Creates a machine resource in Gold.
	// *
	// * @param mach
	// * the machine to create
	// */
	// @POST
	// @Path("/machines")
	// @Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public void createMachine(Machine mach);
	//
	// /**
	// * Gets details about the machine in question from Gold.
	// *
	// * @param machineName
	// * the name of the machine
	// * @return the machine details
	// */
	// @GET
	// @Path("/machines/{machineName}")
	// @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public Machine getMachine(@PathParam("machineName") String machineName);
	//
	// /**
	// * Modifies an existing Machine within Gold.
	// *
	// * @param machName
	// * the name of the machine
	// * @param machine
	// * the new details of this machine. Empty values in this will not
	// * delete existing values in Gold database.
	// * @return the new machine object as it is stored in Gold
	// */
	// @POST
	// @Path("/machines/{machineName}")
	// @Consumes({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
	// public Machine modifyMachine(@PathParam("machineName") String machName,
	// Machine machine);
	//
	// /**
	// * Only for development, batch-deletes a specific resource type
	// *
	// * @param type
	// * the name of the resource
	// */
	// @DELETE
	// @Path("/develop/deleteall/{resourceType}")
	// public void delete(@PathParam("resourceType") String type);

}
