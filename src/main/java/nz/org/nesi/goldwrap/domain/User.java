package nz.org.nesi.goldwrap.domain;

import javax.xml.bind.annotation.XmlRootElement;

import nz.org.nesi.goldwrap.errors.UserFault;

import org.apache.commons.lang3.StringUtils;

/**
 * The model for a user.
 * 
 * @author markus
 * 
 */
@XmlRootElement
public class User {

	private String userId;
	private String firstName = "";
	private String middleName = "";
	private String lastName = "";
	private String institution = "";
	private String department = "";
	private String phone = "";
	private String position = "";
	private String email = "";
	private String address = "";
	private String nationality = "New Zealand";
	private String altEmail = "";
	private String preferredClient = "";

	public User() {

	}

	/**
	 * The postal address.
	 * 
	 * @return the postal address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * An (optional) alternative email address.
	 * 
	 * @return the alternative email address
	 */
	public String getAltEmail() {
		return altEmail;
	}

	/**
	 * The department this user is member of.
	 * 
	 * @return the department
	 */
	public String getDepartment() {
		return department;
	}

	/**
	 * The user's current Tuakiri identity, i.e. as an email address
	 * username@homeinstitution (required).
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
	public String getFirstName() {
		return firstName;
	}

	/**
	 * The users' institution (required).
	 * 
	 * @return the name of the institution
	 */
	public String getInstitution() {
		return institution;
	}

	/**
	 * The last name of the user (required).
	 * 
	 * @return the last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * The middle name of the user (optional).
	 * 
	 * @return the middle name
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * The nationality of the user (required).
	 * 
	 * @return the name of the country
	 */
	public String getNationality() {
		return nationality;
	}

	/**
	 * The users phone number (required).
	 * 
	 * @return the phone number
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * The position of the user.
	 * 
	 * @return the postion
	 */
	public String getPosition() {
		return position;
	}

	/**
	 * The preferred client for the user.
	 * 
	 * @return the client
	 */
	public String getPreferredClient() {
		return preferredClient;
	}

	/**
	 * The user id, this is the Tuakiri unique hash id.
	 * 
	 * @return the id
	 */
	public String getUserId() {
		return userId;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setAltEmail(String altEmail) {
		this.altEmail = altEmail;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public void setPreferredClient(String preferredClient) {
		this.preferredClient = preferredClient;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void validate(boolean force) {

		if (StringUtils.isBlank(userId)) {
			throw new UserFault(this, "Invalid user.",
					"UserId field can't be blank.");
		}

		if (!force) {
			return;
		}

		if (StringUtils.isBlank(getEmail())) {
			throw new UserFault(this, "Invalid user " + userId + ".",
					"No email address provided.");
		}

		if (StringUtils.isBlank(getPhone())) {
			throw new UserFault(this, "Invalid user " + userId + ".",
					"No phone number provided.");
		}

		if (StringUtils.isBlank(getInstitution())) {
			throw new UserFault(this, "Invalid user " + userId + ".",
					"No institution provided.");
		}
		if (StringUtils.isBlank(getNationality())) {
			throw new UserFault(this, "Invalid user " + userId + ".",
					"No nationality provided");
		}
		if (StringUtils.isBlank(getAddress())) {
			throw new UserFault(this, "Invalid user " + userId + ".",
					"No address provided");
		}

	}

}
