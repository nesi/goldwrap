package nz.org.nesi.goldwrap.errors;

import javax.xml.ws.WebFault;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

@WebFault(name = "ServiceException", faultBean = "nz.org.nesi.goldwrap.errors.FaultInfo")
public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = -339330063981442247L;
	protected FaultInfo faultInfo = null;

	public ServiceException(String message, FaultInfo faultInfo2) {
		super(message);
		this.faultInfo = faultInfo2;
	}

	public ServiceException(String message, FaultInfo faultInfo2,
			Throwable cause) {
		this(message, faultInfo2);
		if (StringUtils.isBlank(getFaultInfo().getException())) {
			getFaultInfo().setException(ExceptionUtils.getStackTrace(cause));
		}
	}

	public ServiceException(String message, String reason) {
		this(message, new FaultInfo());
		getFaultInfo().setReason(reason);
	}

	public FaultInfo getFaultInfo() {
		return faultInfo;
	}

}
