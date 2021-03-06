package nz.org.nesi.goldwrap.errors;

import javax.xml.ws.WebFault;

import nz.org.nesi.goldwrap.domain.Machine;

/**
 * Thrown when something machine related is not correct (machinename not valid,
 * machine not found...)
 * 
 * @author markus
 * 
 */
@WebFault(name = "MachineFault")
public class MachineFault extends ServiceException {

	private static final long serialVersionUID = 1L;
	private static final Integer ERROR_CODE = 400;

	public MachineFault(Machine mach, String message, String reason) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(ERROR_CODE);
		getFaultInfo().setReason(reason);
		getFaultInfo().setResource(mach);

	}

	public MachineFault(String message, FaultInfo faultInfo) {
		super(message, faultInfo);
	}

	public MachineFault(String message, FaultInfo faultInfo, Throwable cause) {
		super(message, faultInfo, cause);
	}

	public MachineFault(String message, String reason, Integer errorCode) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(errorCode);
		getFaultInfo().setReason(reason);

	}

	public MachineFault(Machine m, String message, String reason,
			Integer errorCode) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(errorCode);
		getFaultInfo().setReason(reason);
		getFaultInfo().setResource(m);

	}

	@Override
	public FaultInfo getFaultInfo() {
		return faultInfo;
	}

}
