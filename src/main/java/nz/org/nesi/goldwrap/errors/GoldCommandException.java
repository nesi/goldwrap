package nz.org.nesi.goldwrap.errors;


import javax.xml.ws.WebFault;

import nz.org.nesi.goldwrap.domain.ExternalCommand;

@WebFault
public class GoldCommandException extends ServiceException {

	private static final long serialVersionUID = 1L;
	private static final Integer ERROR_CODE = 500;

	public GoldCommandException(String message, ExternalCommand ec) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(ERROR_CODE);
		getFaultInfo().setResource(ec);

	}

	public GoldCommandException(String message, FaultInfo faultInfo) {
		super(message, faultInfo);
	}

	public GoldCommandException(String message, FaultInfo faultInfo,
			Throwable cause) {
		super(message, faultInfo, cause);
	}

	public GoldCommandException(String message, String reason,
			ExternalCommand ec) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(ERROR_CODE);
		getFaultInfo().setReason(reason);
		getFaultInfo().setResource(ec);

	}

	public GoldCommandException(String message, String reason, Integer errorCode) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(errorCode);
		getFaultInfo().setReason(reason);

	}

	@Override
	public FaultInfo getFaultInfo() {
		return faultInfo;
	}

}
