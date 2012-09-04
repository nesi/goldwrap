package nz.org.nesi.goldwrap.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import nz.org.nesi.goldwrap.errors.AllocationFault;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

/**
 * Class to model Allocations.
 * 
 * @author markus
 * 
 */
@XmlRootElement
public class Allocation {

	public final static List<String> CLASSES = ImmutableList.of(
			"ProposalDevelopment", "ResearchFunded", "ResearchUnfunded",
			"Private", "Collaborator");

	private Integer allocation = -1;
	private Integer rechargemonths = -1;
	private Integer recharge = -1;
	private Integer startmonth = -1;
	private Integer startyear = -1;

	private String clazz = null;

	private List<Machine> machines = null;

	/**
	 * Total allocation amount for all rechargemonths specified.
	 * 
	 * @return the allocation
	 */
	public Integer getAllocation() {
		return allocation;
	}

	public String getClazz() {
		return clazz;
	}

	/**
	 * The site where the allocation is to be used.
	 * 
	 * @return the name of the site
	 */
	public List<Machine> getMachines() {
		return machines;
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

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public void setMachines(List<Machine> machines) {
		this.machines = machines;
	}

	public void setRecharge(Integer recharge) {
		this.recharge = recharge;
	}

	public void setRechargemonths(Integer rechargemonths) {
		this.rechargemonths = rechargemonths;
	}

	public void setStartmonth(Integer startmonth) {
		this.startmonth = startmonth;
	}

	public void setStartyear(Integer startyear) {
		this.startyear = startyear;
	}

	public void validate(boolean fullCheck) {

		if (StringUtils.isBlank(clazz)) {
			throw new AllocationFault(this, "Invalid Allocation.",
					"'clazz' must be specified");
		}

		if (!CLASSES.contains(clazz)) {
			throw new AllocationFault(this, "Invalid Allocation.", clazz
					+ " not a valid clazz");
		}

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

		if (machines == null || machines.size() == 0) {
			throw new AllocationFault(this, "Invalid allocation.",
					"No machine(s) specified");
		}

		if (!fullCheck) {
			return;
		}

	}

}
