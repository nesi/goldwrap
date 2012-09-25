package nz.org.nesi.goldwrap.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import nz.org.nesi.goldwrap.errors.ProjectFault;

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
	private String description = "";
	private List<String> users = Lists.newLinkedList();

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public void setDescription(String projectDescription) {
		this.description = projectDescription;
	}

	public String getDescription() {
		return description;
	}

	private static final Logger myLogger = LoggerFactory
			.getLogger(Project.class);

	public Project() {
	}

	public Project(String name) {
		this.projectId = name;
	}

	/**
	 * The project id within Gold.
	 */
	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public void validate(boolean fullCheck) {

		if (StringUtils.isBlank(getProjectId())) {
			throw new ProjectFault(this, "Invalid project.",
					"Project name can't be blank.");
		}

	}
}
