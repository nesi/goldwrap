package nz.org.nesi.goldwrap.errors;

import javax.xml.ws.WebFault;

import nz.org.nesi.goldwrap.domain.User;

/**
 * Thrown when something user related is not correct (username not valid, user
 * not found, nationality property missing ...)
 * 
 * @author markus
 * 
 */
@WebFault(name = "UserFault")
public class UserFault extends ServiceException {

	private static final long serialVersionUID = 1L;
	private static final Integer ERROR_CODE = 400;

	public UserFault(String message, FaultInfo faultInfo) {
		super(message, faultInfo);
	}

	public UserFault(String message, FaultInfo faultInfo, Throwable cause) {
		super(message, faultInfo, cause);
	}

	public UserFault(String message, String reason, Integer errorCode) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(errorCode);
		getFaultInfo().setReason(reason);

	}

	public UserFault(User user, String message, String reason) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(ERROR_CODE);
		getFaultInfo().setReason(reason);
		getFaultInfo().setResource(user);

	}

	@Override
	public FaultInfo getFaultInfo() {
		return faultInfo;
	}

}
