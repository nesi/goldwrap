package nz.org.nesi.goldwrap.errors;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FaultInfo implements Serializable {

	private final static long serialVersionUID = 1L;

	protected Integer errorCode;
	protected String reason = "n/a";
	protected Object resource;
	protected String exception = "n/a";

	/**
	 * Gets the value of the http errorCode property.
	 * 
	 * @return possible object is {@link Integer }
	 * 
	 */
	public Integer getErrorCode() {
		return errorCode;
	}

	/**
	 * Gets the stacktrace of the exception for debug purposes.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getException() {
		return exception;
	}

	/**
	 * The reason of the failure.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * The resource that was related to the failure.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */

	public Object getResource() {
		return resource;
	}

	/**
	 * Sets the value of the errorCode property.
	 * 
	 * @param value
	 *            allowed object is {@link Integer }
	 * 
	 */
	public void setErrorCode(Integer value) {
		this.errorCode = value;
	}

	/**
	 * Sets the value of the exception property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setException(String value) {
		this.exception = value;
	}

	/**
	 * Sets the value of the reason property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setReason(String value) {
		this.reason = value;
	}

	/**
	 * Sets the value of the resource property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setResource(Object value) {
		this.resource = value;
	}
}
