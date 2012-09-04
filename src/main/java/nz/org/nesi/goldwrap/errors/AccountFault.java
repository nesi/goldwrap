package nz.org.nesi.goldwrap.errors;

import javax.xml.ws.WebFault;

import nz.org.nesi.goldwrap.domain.Account;

/**
 * Thrown when something account related is not correct (accountId not valid,
 * account not found...)
 * 
 * @author markus
 * 
 */
@WebFault(name = "AccountFault")
public class AccountFault extends ServiceException {

	private static final long serialVersionUID = 1L;
	private static final Integer ERROR_CODE = 400;

	public AccountFault(Account all, String message, String reason) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(ERROR_CODE);
		getFaultInfo().setReason(reason);
		getFaultInfo().setResource(all);

	}

	public AccountFault(String message, FaultInfo faultInfo) {
		super(message, faultInfo);
	}

	public AccountFault(String message, FaultInfo faultInfo, Throwable cause) {
		super(message, faultInfo, cause);
	}

	public AccountFault(String message, String reason, Integer errorCode) {
		super(message, new FaultInfo());
		getFaultInfo().setErrorCode(errorCode);
		getFaultInfo().setReason(reason);

	}

	public AccountFault(Account a, String message, String reason,
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
