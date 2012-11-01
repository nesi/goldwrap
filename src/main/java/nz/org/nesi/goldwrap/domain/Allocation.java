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

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public List<String> getMachines() {
		return machines;
	}

	public void setMachines(List<String> machines) {
		this.machines = machines;
	}

	private Allocation() {

	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public long getCreditLimit() {
		return creditLimit;
	}

	public void setCreditLimit(long creditLimit) {
		this.creditLimit = creditLimit;
	}

	public long getDeposited() {
		return deposited;
	}

	public void setDeposited(long deposited) {
		this.deposited = deposited;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
