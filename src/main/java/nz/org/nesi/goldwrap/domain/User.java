package nz.org.nesi.goldwrap.domain;

import javax.xml.bind.annotation.XmlRootElement;

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
	private String email;
	private String address;
	private String nationality = "New Zealand";

	public User() {

	}

	public String getAddress() {
		return address;
	}

	public String getDepartment() {
		return department;
	}

	public String getEmail() {
		return email;
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

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
