package nz.org.nesi.goldwrap.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import nz.org.nesi.goldwrap.errors.ProjectFault;
import nz.org.nesi.goldwrap.util.GoldHelper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	// private String site = "";

	private List<Integer> accountIds = Lists.newArrayList();

	private static final Logger myLogger = LoggerFactory
			.getLogger(Project.class);

	public Project() {
	}

	public Project(String name) {
		this.projectId = name;
	}

	public void addAccountId(Integer id) {
		accountIds.add(id);
	}

	public Account getAccount(String site) {

		Account acc = GoldHelper.getAccount(this, site);
		return acc;
	}

	/**
	 * Only used internally, this one doesn't need to be specified when creating
	 * a project.
	 * 
	 * @return the id of the account this project is linked to
	 */
	public List<Integer> getAccountIds() {
		return accountIds;
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
	 * A list of userIds for users who are members of this project.
	 */
	public List<User> getUsers() {
		return users;
	}

	// /**
	// * The site where the project will be run.
	// *
	// * @return the name of the site
	// */
	// public String getSite() {
	// return site;
	// }

	/**
	 * Whether this project is funded or not (defaults to: False).
	 * 
	 * @return whether funded or not
	 */
	public Boolean isFunded() {
		return funded;
	}

	private List<Account> queryAccounts() {

		List<Account> accs = GoldHelper.getAllAccounts(this);

		return accs;

	}

	public void setAccountIds(List<Integer> accountIds) {
		this.accountIds = accountIds;
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

	// public void setSite(String site) {
	// this.site = site;
	// }

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
