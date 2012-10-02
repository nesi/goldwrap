package nz.org.nesi.goldwrap.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

/**
 * Model for an account.
 * 
 * @author markus
 * 
 */
@XmlRootElement
public class Account {

	private String name = "";

	private Integer accountId = -1;

	private List<String> projects = null;

	private List<String> users = Lists.newArrayList();
	private List<String> machines = Lists.newArrayList();
	private String description = "";
	private String site = null;
	private String clazz = "";

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	private int amount = 0;

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

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
	 * Returns all machines that are associated with this account.
	 * 
	 * @return the machines
	 */
	public List<String> getMachines() {
		return machines;
	}

	public String getName() {
		return name;
	}

	/**
	 * The projects this account is associated with.
	 * 
	 * @return the projects
	 */
	public List<String> getProjects() {
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
	public List<String> getUsers() {
		return users;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setMachines(List<String> machines) {
		this.machines = machines;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProjects(List<String> project) {
		this.projects = project;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

}
