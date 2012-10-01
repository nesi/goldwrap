package nz.org.nesi.goldwrap.domain;

import javax.xml.bind.annotation.XmlRootElement;

import nz.org.nesi.goldwrap.errors.MachineFault;

import org.apache.commons.lang3.StringUtils;

/**
 * The model for a machine.
 * 
 * @author markus
 * 
 */
@XmlRootElement
public class Machine {

	private String name = "";

	private String arch = "";

	private String opsys = "";

	private String description = "";

	// private String organization = "";

	// public String getOrganization() {
	// return organization;
	// }
	//
	// public void setOrganization(String organization) {
	// this.organization = organization;
	// }

	public Machine() {

	}

	public Machine(String name) {
		this.name = name;
	}

	/**
	 * The architecture of this machine.
	 * 
	 * @param arch
	 *            the architecture
	 */
	public String getArch() {
		return arch;
	}

	/**
	 * A description of this machine.
	 * 
	 * @param description
	 *            the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * The name of this machine.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * The operating system of this machine.
	 * 
	 * @return the operating system.
	 */
	public String getOpsys() {
		return opsys;
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOpsys(String opsys) {
		this.opsys = opsys;
	}

	public void validate(boolean fullCheck) {

		if (StringUtils.isBlank(getName())) {
			throw new MachineFault(this, "Invalid project.",
					"Project name can't be blank.");
		}

		if (!fullCheck) {
			return;
		}

	}

}
