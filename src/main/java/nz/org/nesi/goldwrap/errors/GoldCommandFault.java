package nz.org.nesi.goldwrap.errors;

import javax.xml.ws.WebFault;

import nz.org.nesi.goldwrap.domain.ExternalCommand;

/**
 * Error object that is thrown if a gold command exits with a non-zero exit
 * code.
 * 
 * @author markus
 * 
 */
@WebFault(name = "GoldCommandFault")
public class GoldCommandFault extends ServiceException {

	private static final long serialVersionUID = 1L;
	private static final Integer ERROR_CODE = 500;

	public GoldCommandFault(String message, ExternalCommand ec) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(ERROR_CODE);
		getFaultInfo().setResource(ec);

	}

	public GoldCommandFault(String message, FaultInfo faultInfo) {
		super(message, faultInfo);
	}

	public GoldCommandFault(String message, FaultInfo faultInfo, Throwable cause) {
		super(message, faultInfo, cause);
	}

	public GoldCommandFault(ExternalCommand ec, String message, String reason) {
		this(ec, message, reason, ERROR_CODE);
	}

	public GoldCommandFault(ExternalCommand cmd, String message, String reason,
			Integer errorCode) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(errorCode);
		getFaultInfo().setReason(reason);
		getFaultInfo().setResource(cmd);

	}

	public GoldCommandFault(String message, String reason) {
		this(null, message, reason, ERROR_CODE);
	}

	@Override
	public FaultInfo getFaultInfo() {
		return faultInfo;
	}

}
