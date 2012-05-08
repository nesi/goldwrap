package nz.org.nesi.goldwrap.api;

import javax.jws.WebService;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

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
	public void createUser(User user);

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
	 * Returns registration details about this user by querying Gold.
	 * 
	 * @param username
	 *            the username
	 * @return the {@link User} object
	 */
	@GET
	@Path("/users/{username}")
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
	public void modifyUser(@PathParam("username") String username, User user);

}
