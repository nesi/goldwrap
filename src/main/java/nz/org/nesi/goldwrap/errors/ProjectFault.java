package nz.org.nesi.goldwrap.errors;

import javax.xml.ws.WebFault;

import nz.org.nesi.goldwrap.domain.Project;

/**
 * Thrown when something project related is not correct (projectname not valid,
 * project not found...)
 * 
 * @author markus
 * 
 */
@WebFault(name = "ProjectFault")
public class ProjectFault extends ServiceException {

	private static final long serialVersionUID = 1L;
	private static final Integer ERROR_CODE = 400;

	public ProjectFault(Project proj, String message, String reason) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(ERROR_CODE);
		getFaultInfo().setReason(reason);
		getFaultInfo().setResource(proj);

	}

	public ProjectFault(String message, FaultInfo faultInfo) {
		super(message, faultInfo);
	}

	public ProjectFault(String message, FaultInfo faultInfo, Throwable cause) {
		super(message, faultInfo, cause);
	}

	public ProjectFault(String message, String reason) {
		this(message, reason, ERROR_CODE);
	}

	public ProjectFault(String message, String reason, Integer errorCode) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(errorCode);
		getFaultInfo().setReason(reason);

	}

	public ProjectFault(Project p, String message, String reason,
			Integer errorCode) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(errorCode);
		getFaultInfo().setReason(reason);
		getFaultInfo().setResource(p);

	}

	@Override
	public FaultInfo getFaultInfo() {
		return faultInfo;
	}

}
