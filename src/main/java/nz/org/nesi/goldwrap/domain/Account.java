package nz.org.nesi.goldwrap.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model for an account.
 * 
 * @author markus
 * 
 */
@XmlRootElement
public class Account {

	private Integer accountId = -1;
	private List<Project> projects = null;
	private List<User> users = null;
	private List<Machine> machines = null;

	/**
	 * Returns all machines that are associated with this account.
	 * 
	 * @return the machines
	 */
	public List<Machine> getMachines() {
		return machines;
	}

	public void setMachines(List<Machine> machines) {
		this.machines = machines;
	}

	private String description = "";
	private String site = null;

	public Account() {
	}

	public Account(Integer id) {
		this.accountId = id;
	}

	/**
	 * The account id in Gold
	 * 
	 * @return the id in gold
	 */
	public Integer getAccountId() {
		return accountId;
	}

	/**
	 * A description for this account.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * The projects this account is associated with.
	 * 
	 * @return the projects
	 */
	public List<Project> getProjects() {
		return projects;
	}

	public String getSite() {
		return site;
	}

	/**
	 * The users this account is associated with.
	 * 
	 * @return the users
	 */
	public List<User> getUsers() {
		return users;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setProjects(List<Project> project) {
		this.projects = project;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

}
