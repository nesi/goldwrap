package nz.org.nesi.goldwrap.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import nz.org.nesi.goldwrap.errors.ProjectFault;

import org.apache.commons.lang3.StringUtils;

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
	private List<User> users;
	private List<Allocation> allocations;
	private String projectHistory = "";
	private String principal = "";
	private Boolean funded = false;
	private Long timestamp = -1L;

	public Project() {
	}

	public Project(String name) {
		this.projectId = name;
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

	public String getProjectTitle() {
		return projectTitle;
	}

	/**
	 * Not sure what that is for...
	 * 
	 * @return the timestamp
	 */
	public Long getTimestamp() {
		return timestamp;
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

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
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
