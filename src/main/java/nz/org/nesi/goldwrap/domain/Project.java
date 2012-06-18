package nz.org.nesi.goldwrap.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import nz.org.nesi.goldwrap.errors.ProjectFault;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

/**
 * The model that contains all project specific properties.
 * 
 * 
 * @author markus
 * 
 */
@XmlRootElement
public class Project {

	private String projectId = "";
	private String projectTitle = "";
	private List<User> users = Lists.newArrayList();
	private List<Allocation> allocations = Lists.newArrayList();
	private String projectHistory = "";
	private String principal = "";
	private Boolean funded = false;
	private String site = "";

	private Integer accountId = -1;

	public Project() {
	}

	public Project(String name) {
		this.projectId = name;
	}

	/**
	 * Only used internally, this one doesn't need to be specified when creating
	 * a project.
	 * 
	 * @return the id of the account this project is linked to
	 */
	public Integer getAccountId() {
		return accountId;
	}

	/**
	 * The allocations for this project.
	 */
	public List<Allocation> getAllocations() {
		return allocations;
	}

	/**
	 * The username of the principal investigator - this is the Tuakiri hash.
	 * 
	 * @return the pi username
	 */
	public String getPrincipal() {
		return principal;
	}

	/**
	 * Other data, to be changed later on.
	 */
	public String getProjectHistory() {
		return projectHistory;
	}

	/**
	 * The project id within Gold.
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * A human readable title.
	 * 
	 * @return the project title
	 */
	public String getProjectTitle() {
		return projectTitle;
	}

	/**
	 * The site where the project will be run.
	 * 
	 * @return the name of the site
	 */
	public String getSite() {
		return site;
	}

	/**
	 * A list of userIds for users who are members of this project.
	 */
	public List<User> getUsers() {
		return users;
	}

	/**
	 * Whether this project is funded or not (defaults to: False).
	 * 
	 * @return whether funded or not
	 */
	public Boolean isFunded() {
		return funded;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public void setAllocations(List<Allocation> allocations) {
		this.allocations = allocations;
	}

	public void setFunded(Boolean funded) {
		this.funded = funded;
	}

	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	public void setProjectHistory(String projectData) {
		this.projectHistory = projectData;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public void setProjectTitle(String projectTitle) {
		this.projectTitle = projectTitle;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public void validate(boolean fullCheck) {

		if (StringUtils.isBlank(getProjectId())) {
			throw new ProjectFault(this, "Invalid project.",
					"Project name can't be blank.");
		}

		if (!fullCheck) {
			return;
		}

	}
}
