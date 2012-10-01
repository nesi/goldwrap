package nz.org.nesi.goldwrap.domain;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The model for a user.
 * 
 * @author markus
 * 
 */
@XmlRootElement
public class User {

	private String userId;
	private String fullName = "";
	private String phone = "";
	private String email = "";
	private String organization;
	private String affiliation = "";

	public User() {

	}

	/**
	 * No clue what that is supposed to be, ask Andrew.
	 * 
	 * @return the affiliation
	 */
	public String getAffiliation() {
		return affiliation;
	}

	/**
	 * The user's email address.
	 * 
	 * @return the email address
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * The first name of the user (required).
	 * 
	 * @return first name
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * The organization this user belongs to.
	 * 
	 * @return the name of the organization
	 */
	public String getOrganization() {
		return organization;
	}

	// /**
	// * The users' institution..
	// *
	// * @return the name of the institution
	// */
	// public String getInstitution() {
	// return institution;
	// }

	/**
	 * The users phone number (required).
	 * 
	 * @return the phone number
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * The user id, this is the Tuakiri unique hash id.
	 * 
	 * @return the id
	 */
	public String getUserId() {
		return userId;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	// public void setInstitution(String institution) {
	// this.institution = institution;
	// }

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setOrganization(String org) {
		this.organization = org;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
