package nz.org.nesi.goldwrap.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

/**
 * Class to model Allocations.
 * 
 * @author markus
 * 
 */
@XmlRootElement
public class Allocation {

	private int allocationId;

	private int accountId;

	private boolean active = true;

	private String startDate;
	private String endDate;
	private long amount;
	private long creditLimit;
	private long deposited;
	private String description;
	private List<String> machines = Lists.newArrayList();
	private String clazz = "";
	private Allocation() {

	}
	public int getAccountId() {
		return accountId;
	}

	public int getAllocationId() {
		return allocationId;
	}

	public long getAmount() {
		return amount;
	}

	public String getClazz() {
		return clazz;
	}

	public long getCreditLimit() {
		return creditLimit;
	}

	public long getDeposited() {
		return deposited;
	}

	public String getDescription() {
		return description;
	}

	public String getEndDate() {
		return endDate;
	}

	public List<String> getMachines() {
		return machines;
	}

	public String getStartDate() {
		return startDate;
	}

	public boolean isActive() {
		return active;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setAllocationId(int allocationId) {
		this.allocationId = allocationId;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public void setCreditLimit(long creditLimit) {
		this.creditLimit = creditLimit;
	}

	public void setDeposited(long deposited) {
		this.deposited = deposited;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public void setMachines(List<String> machines) {
		this.machines = machines;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

}
