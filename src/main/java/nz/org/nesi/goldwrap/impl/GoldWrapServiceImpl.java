package nz.org.nesi.goldwrap.impl;

import javax.jws.WebService;
import javax.ws.rs.Path;

import nz.org.nesi.goldwrap.api.GoldWrapService;
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

	public synchronized void initialize() {

		if (!initialized) {
			try {
				myLogger.debug("Initializing...");
			} finally {
				initialized = true;
			}
		}

	}

	public void createUser(User user) {

		String username = user.getUserId();
		String fullname = user.getFullName();
		String email = user.getEmail();
		String phone = user.getPhone();

		GoldWrap.createUser(username, fullname, email, phone);
	}

	public void modifyUser(User user) throws GoldCommandFault {

		String username = user.getUserId();
		String fullname = user.getFullName();
		String email = user.getEmail();
		String phone = user.getPhone();

		GoldWrap.modifyUser(username, fullname, email, phone);

	}

	public void deleteUser(String username) {

		GoldWrap.deleteUser(username);

	}
}
