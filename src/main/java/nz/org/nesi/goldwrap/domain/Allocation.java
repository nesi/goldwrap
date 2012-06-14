package nz.org.nesi.goldwrap.domain;

import javax.xml.bind.annotation.XmlRootElement;

import nz.org.nesi.goldwrap.errors.AllocationFault;

import org.apache.commons.lang3.StringUtils;

/**
 * Class to model Allocations.
 * 
 * @author markus
 * 
 */
@XmlRootElement
public class Allocation {

	private Integer allocation = -1;
	private Integer rechargemonths = -1;
	private Integer recharge = -1;
	private Integer startmonth = -1;
	private Integer startyear = -1;

	private String site = "";

	/**
	 * Allocation per recharge period.
	 * 
	 * @return the allocation
	 */
	public Integer getAllocation() {
		return allocation;
	}

	/**
	 * Number of recharge periods.
	 * 
	 * @return number of recharge periods
	 */
	public Integer getRecharge() {
		return recharge;
	}

	/**
	 * Number of months per recharge period - assume that recharge periods
	 * straddle whole months.
	 * 
	 * @return rechargemonths
	 */
	public Integer getRechargemonths() {
		return rechargemonths;
	}

	/**
	 * Not sure what this is, it's not used in the respective gold command.
	 * 
	 * From the wiki: in the portal, this should be a drop down list using
	 * labels from the guidelines which map to internal names.
	 * 
	 * @return the site
	 */
	public String getSite() {
		return site;
	}

	/**
	 * The startmonth.
	 * 
	 * @return startmonth
	 */
	public Integer getStartmonth() {
		return startmonth;
	}

	/**
	 * The startyear.
	 * 
	 * @return startyear
	 */
	public Integer getStartyear() {
		return startyear;
	}

	public void setAllocation(Integer allocation) {
		this.allocation = allocation;
	}

	public void setRecharge(Integer recharge) {
		this.recharge = recharge;
	}

	public void setRechargemonths(Integer rechargemonths) {
		this.rechargemonths = rechargemonths;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public void setStartmonth(Integer startmonth) {
		this.startmonth = startmonth;
	}

	public void setStartyear(Integer startyear) {
		this.startyear = startyear;
	}

	public void validate(boolean fullCheck) {

		if (allocation < 0) {
			throw new AllocationFault(this, "Invalid Allocation.",
					"Allocation must be > 0");
		}

		if (recharge < 0) {
			throw new AllocationFault(this, "Invalid Allocation.",
					"recharge must be > 0");
		}

		if (rechargemonths < 0) {
			throw new AllocationFault(this, "Invalid Allocation.",
					"rechargemonths must be > 0");
		}

		if (startmonth < 1 || startmonth > 12) {
			throw new AllocationFault(this, "Invalid Allocation.",
					"startmonth must be between 1 and 12");
		}
		if (startyear < 2011) {
			throw new AllocationFault(this, "Invalid Allocation.",
					"startyear be > 2011");
		}

		if (!fullCheck) {
			return;
		}

		if (StringUtils.isBlank(site)) {
			throw new AllocationFault(this, "Invalid Allocation.",
					"site must be specified");
		}

	}

}
