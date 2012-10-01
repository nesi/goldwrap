package nz.org.nesi.goldwrap.errors;

import javax.xml.ws.WebFault;

import nz.org.nesi.goldwrap.domain.Organization;

/**
 * Thrown when something project related is not correct (projectname not valid,
 * project not found...)
 * 
 * @author markus
 * 
 */
@WebFault(name = "OrganizationFault")
public class OrganizationFault extends ServiceException {

	private static final long serialVersionUID = 1L;
	private static final Integer ERROR_CODE = 400;

	public OrganizationFault(Organization org, String message, String reason) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(ERROR_CODE);
		getFaultInfo().setReason(reason);
		getFaultInfo().setResource(org);

	}

	public OrganizationFault(String message, FaultInfo faultInfo) {
		super(message, faultInfo);
	}

	public OrganizationFault(String message, FaultInfo faultInfo,
			Throwable cause) {
		super(message, faultInfo, cause);
	}

	public OrganizationFault(String message, String reason) {
		this(message, reason, ERROR_CODE);
	}

	public OrganizationFault(String message, String reason, Integer errorCode) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(errorCode);
		getFaultInfo().setReason(reason);

	}

	public OrganizationFault(Organization o, String message, String reason,
			Integer errorCode) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(errorCode);
		getFaultInfo().setReason(reason);
		getFaultInfo().setResource(o);

	}

	@Override
	public FaultInfo getFaultInfo() {
		return faultInfo;
	}

}
