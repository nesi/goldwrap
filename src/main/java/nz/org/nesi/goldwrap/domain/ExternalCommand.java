package nz.org.nesi.goldwrap.domain;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import nz.org.nesi.goldwrap.errors.GoldCommandFault;

import org.apache.commons.lang3.StringUtils;

@XmlRootElement
public class ExternalCommand {

	private List<String> command;

	private int exitCode = -1;

	private List<String> stdOut;

	private List<String> stdErr;

	private Date executed;

	private Date finished;

	private int expectedExitCode = -1;

	private ExternalCommand() {
	}

	public ExternalCommand(List<String> command) {
		this.command = command;
	}

	public ExternalCommand(List<String> command, int expectedExitCode) {
		this.expectedExitCode = expectedExitCode;
	}

	public List<String> getCommand() {
		return command;
	}

	public Date getExecuted() {
		return executed;
	}

	public int getExitCode() {
		return exitCode;
	}

	public int getExpectedExitCode() {
		return expectedExitCode;
	}

	public Date getFinished() {
		return finished;
	}

	public List<String> getStdErr() {
		return stdErr;
	}

	public List<String> getStdOut() {
		return stdOut;
	}

	public void setCommand(List<String> command) {
		this.command = command;
	}

	public void setExecuted(Date executed) {
		this.executed = executed;
	}

	public void setExitCode(int ec) {
		this.exitCode = ec;
	}

	public void setExpectedExitCode(int expectedExitCode) {
		this.expectedExitCode = expectedExitCode;
	}

	public void setFinished(Date finished) {
		this.finished = finished;
	}

	public void setStdErr(List<String> stdErr) {
		this.stdErr = stdErr;
	}

	public void setStdOut(List<String> stdOut) {
		this.stdOut = stdOut;
	}

	@Override
	public String toString() {
		return StringUtils.join(getCommand());
	}

	public void verify() {

		if (expectedExitCode >= 0) {
			if (getExitCode() != expectedExitCode) {
				throw new GoldCommandFault(
						"Expected exit code differs from actual exit code.",
						this);
			}
		}

		if (getStdErr().size() > 0) {
			throw new GoldCommandFault(
					"Stderr not empty, assuming this is an error.", this);
		}

	}

	public boolean wasExecuted() {
		return executed != null;
	}

	public boolean wasSuccessful() {
		if (wasExecuted()) {
			if (exitCode == 0) {
				return true;
			}
		}
		return false;
	}
}
