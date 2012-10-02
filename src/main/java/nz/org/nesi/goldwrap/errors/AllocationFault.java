package nz.org.nesi.goldwrap.errors;

import javax.xml.ws.WebFault;

import nz.org.nesi.goldwrap.domain.DepositAllocation;

/**
 * Thrown when something machine related is not correct (machinename not valid,
 * machine not found...)
 * 
 * @author markus
 * 
 */
@WebFault(name = "AllocationFault")
public class AllocationFault extends ServiceException {

	private static final long serialVersionUID = 1L;
	private static final Integer ERROR_CODE = 400;

	public AllocationFault(DepositAllocation all, String message, String reason) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(ERROR_CODE);
		getFaultInfo().setReason(reason);
		getFaultInfo().setResource(all);

	}

	public AllocationFault(String message, FaultInfo faultInfo) {
		super(message, faultInfo);
	}

	public AllocationFault(String message, FaultInfo faultInfo, Throwable cause) {
		super(message, faultInfo, cause);
	}

	public AllocationFault(String message, String reason, Integer errorCode) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(errorCode);
		getFaultInfo().setReason(reason);

	}

	public AllocationFault(DepositAllocation a, String message, String reason,
			Integer errorCode) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(errorCode);
		getFaultInfo().setReason(reason);
		getFaultInfo().setResource(a);

	}

	@Override
	public FaultInfo getFaultInfo() {
		return faultInfo;
	}

}
