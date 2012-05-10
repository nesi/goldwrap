package nz.org.nesi.goldwrap.domain;

import javax.xml.bind.annotation.XmlRootElement;

import nz.org.nesi.goldwrap.errors.UserFault;

import org.apache.commons.lang3.StringUtils;

@XmlRootElement
public class User {

	private String userId;
	private String firstName;
	private String middleName;
	private String lastName;
	private String institution;
	private String department;
	private String phone;
	private String position;
	private String altEmail = "";
	private String address;
	private String nationality = "New Zealand";

	public User() {

	}

	public String getAddress() {
		return address;
	}

	public String getAltEmail() {
		return altEmail;
	}

	public String getDepartment() {
		return department;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getInstitution() {
		return institution;
	}

	public String getLastName() {
		return lastName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public String getNationality() {
		return nationality;
	}

	public String getPhone() {
		return phone;
	}

	public String getPosition() {
		return position;
	}

	public String getUserId() {
		return userId;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setAltEmail(String email) {
		this.altEmail = email;
	}

	public void setDepartment(String department) {
		this.department = department;
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

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void validate() {

		if (StringUtils.isBlank(userId)) {
			throw new UserFault(this, "Invalid user.",
					"UserId field can't be blank.");
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
