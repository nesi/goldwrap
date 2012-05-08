package nz.org.nesi.goldwrap.impl;

import javax.jws.WebService;
import javax.ws.rs.Path;

import nz.org.nesi.goldwrap.api.GoldWrapService;
import nz.org.nesi.goldwrap.domain.ExternalCommand;
import nz.org.nesi.goldwrap.domain.User;
import nz.org.nesi.goldwrap.errors.ServiceException;
import nz.org.nesi.goldwrap.errors.UserFault;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

@WebService(endpointInterface = "nz.org.nesi.goldwrap.api.GoldWrapService", name = "GoldWrapService")
@Path("/goldwrap")
public class GoldWrapServiceImpl implements GoldWrapService {

	private static ObjectMapper mapper = null;

	private static ExternalCommand executeGoldCommand(String command) {
		ExternalCommand gc = new ExternalCommand(command);
		gc.execute();
		gc.verify();
		return gc;
	}

	private static ObjectMapper getMapper() {
		if (mapper == null) {
			AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
			AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
			AnnotationIntrospector pair = new AnnotationIntrospector.Pair(
					primary, secondary);

			mapper = new ObjectMapper();
			AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
			// make deserializer use JAXB annotations (only)
			mapper.getDeserializationConfig().withAnnotationIntrospector(pair);
			// make serializer use JAXB annotations (only)
			mapper.getSerializationConfig().withAnnotationIntrospector(pair);

		}
		return mapper;
	}

	private void checkUsername(String username) {
		if (StringUtils.isBlank(username)) {
			throw new ServiceException("Can't execute operation.",
					"Username blank or not specified.");
		}
	}

	private <T> T convertFromJSONString(String json, Class<T> objectType) {
		try {
			return getMapper().readValue(json, objectType);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private String convertToJSONString(Object o) {

		try {
			return getMapper().writeValueAsString(o);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void createUser(User user) {

		String username = user.getUserId();
		if (StringUtils.isBlank(username)) {
			throw new UserFault(user, "Can't create user.",
					"UserId field can't be blank.");
		}

		String phone = user.getPhone();
		if (StringUtils.isBlank(phone)) {
			throw new UserFault(user, "Can't create user " + username + ".",
					"No phone number provided.");
		}

		String fullName = user.getLastName() + ", " + user.getFirstName();
		String email = user.getEmail();

		if (isRegistered(username)) {
			throw new UserFault("Can't create user.", "User " + username
					+ " already in Gold database.", 409);
		}

		String desc = convertToJSONString(user);

		String command = "gmkuser -n \"" + fullName + "\" -E " + email
				+ " -d '" + desc + "' -F " + phone + " " + username;

		ExternalCommand ec = executeGoldCommand(command);

		if (!isRegistered(username)) {
			throw new UserFault(user, "Can't create user.", "Unknown reason");
		}

	}

	public void deleteUser(String username) {

		if (StringUtils.isBlank(username)) {
			throw new ServiceException("Can't delete user.",
					"Username blank or not specified.");
		}

		if (!isRegistered(username)) {
			throw new UserFault("Can't delete user.", "User " + username
					+ " not in Gold database.", 404);
		}

		String command = "grmuser " + username;
		ExternalCommand ec = executeGoldCommand(command);

		if (isRegistered(username)) {
			throw new UserFault("Could not delete user.", "Unknown reason.",
					500);
		}

	}

	private <T> T extractObject(Class<T> dtoClass, String line) {
		int index = line.indexOf("{");
		String temp = line.substring(index);
		System.out.println(temp);
		T result = convertFromJSONString(temp, dtoClass);
		return result;
	}

	public User getUser(String username) {

		checkUsername(username);

		ExternalCommand ec = executeGoldCommand("glsuser -show Description "
				+ username + " --quiet");

		if (ec.getStdOut().size() == 0) {
			throw new UserFault("Can't retrieve user.", "User " + username
					+ " not in Gold database.", 404);
		}

		User u = extractObject(User.class, ec.getStdOut().get(0));
		if (!username.equals(u.getUserId())) {
			throw new ServiceException("Internal error",
					"Gold userId and userId in description don't match for user '"
							+ username + "'");
		}
		return u;

	}

	public boolean isRegistered(String username) {

		ExternalCommand gc = executeGoldCommand("glsuser -show Name -quiet");

		if (gc.getStdOut().contains(username)) {
			return true;
		} else {
			return false;
		}
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

		if (!isRegistered(username)) {
			throw new UserFault("Can't modify user.", "User " + username
					+ " not in Gold database.", 404);
		}

		String phone = user.getPhone();
		if (StringUtils.isBlank(phone)) {
			throw new UserFault(user, "Can't create user " + username + ".",
					"No phone number provided.");
		}

		String fullName = user.getLastName() + ", " + user.getFirstName();
		String email = user.getEmail();

		String desc = convertToJSONString(user);

		String command = "gchuser -n \"" + fullName + "\" -E " + email
				+ " -d '" + desc + "' -F " + phone + " " + username;

		ExternalCommand ec = executeGoldCommand(command);

		if (!isRegistered(username)) {
			throw new UserFault(user, "Can't create user.", "Unknown reason");
		}

	}
}
