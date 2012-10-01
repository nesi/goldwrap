package nz.org.nesi.goldwrap.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Organization {

	private String name;
	private String description = "";

	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
