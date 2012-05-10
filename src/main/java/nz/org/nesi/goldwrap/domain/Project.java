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

	private String projectId;
	private List<String> users;

	private String projectClass;
	private int allocation;
	private int rechargeMonths;
	private int recharge;
	private int startMonth;
	private int startYear;
	private String site;
	private String projectData;

	public Project() {
	}

	public Project(String name) {
		this.projectId = name;
	}

	/**
	 * The allocation for this project.
	 */
	public int getAllocation() {
		return allocation;
	}

	/**
	 * No idea what that is.
	 */
	public String getProjectClass() {
		return projectClass;
	}

	/**
	 * Other data, to be changed later on.
	 */
	public String getProjectData() {
		return projectData;
	}

	/**
	 * The project id within Gold.
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * No idea whatsoever.
	 */
	public int getRecharge() {
		return recharge;
	}

	/**
	 * I have no clue.
	 */
	public int getRechargeMonths() {
		return rechargeMonths;
	}

	/**
	 * The site where this project will be run.
	 */
	public String getSite() {
		return site;
	}

	/**
	 * The start month of this project.
	 */
	public int getStartMonth() {
		return startMonth;
	}

	/**
	 * The start year of this project.
	 */
	public int getStartYear() {
		return startYear;
	}

	/**
	 * A list of userIds for users who are members of this project.
	 */
	public List<String> getUsers() {
		return users;
	}

	public void setAllocation(int allocation) {
		this.allocation = allocation;
	}

	public void setName(String name) {
		this.projectId = name;
	}

	public void setProjectClass(String projectClass) {
		this.projectClass = projectClass;
	}

	public void setProjectData(String projectData) {
		this.projectData = projectData;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public void setRecharge(int recharge) {
		this.recharge = recharge;
	}

	public void setRechargeMonths(int rechargeMonths) {
		this.rechargeMonths = rechargeMonths;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public void setStartMonth(int startMonth) {
		this.startMonth = startMonth;
	}

	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}

	public void setUsers(List<String> users) {
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

		if (StringUtils.isBlank(getProjectClass())) {
			throw new ProjectFault(this, "Invalid project.",
					"Project class can't be blank.");
		}

		if (StringUtils.isBlank(getSite())) {
			throw new ProjectFault(this, "Invalid project.",
					"Project site can't be blank.");
		}

		// if (getAllocation() == null) {
		// throw new ProjectFault(this, "Invalid project.",
		// "Project allocation can't be blank.");
		// }
		//
		// if (getRechargeMonths() == null) {
		// throw new ProjectFault(this, "Invalid project.",
		// "Project rechargeMonths can't be blank.");
		// }
		//
		// if (getRecharge() == null) {
		// throw new ProjectFault(this, "Invalid project.",
		// "Project recharge can't be blank.");
		// }
		//
		// if (getStartMonth() == null) {
		// throw new ProjectFault(this, "Invalid project.",
		// "Project startMonth can't be blank.");
		// }
		//
		// if (getStartYear() == null) {
		// throw new ProjectFault(this, "Invalid project.",
		// "Project startYear can't be blank.");
		// }

	}
}
