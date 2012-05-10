package nz.org.nesi.goldwrap.domain


import groovy.util.logging.Slf4j;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import nz.org.nesi.goldwrap.errors.GoldCommandException



@XmlRootElement
@Slf4j
class ExternalCommand {

	static boolean useSSH = false

	static hasMany = [stdout : String, stderr : String]

	String command
	int exitCode
	List<String> stdOut
	List<String> stdErr

	Date executed
	Date finished

	public ExternalCommand() {
	}

	public ExternalCommand(String command) {
		this.command = command
	}

	public String command() {
		if ( useSSH ) {
			return 'ssh gold@gold /opt/gold/bin/'+command
		} else {
			return command
		}
	}

	public boolean wasExecuted() {
		return ( executed != null )
	}

	public boolean wasSuccessful() {
		if ( wasExecuted() ) {
			if ( exitCode == 0 ) {
				return true;
			}
		}
		return false;
	}

	public List<String> getStdOut() {
		return stdOut
	}

	public List<String> getStdErr() {
		return stdErr
	}

	public int getExitCode() {
		return exitCode;
	}

	void execute() {

		if ( getExecuted() ) {
			throw new RuntimeException("Command already executed.")
		}

		log.debug("Executing: "+command())

		setExecuted(new Date())
		def proc = command().execute()
		proc.waitFor()
		setFinished(new Date())

		setExitCode(proc.exitValue())
		def stdout = []
		proc.in.text.split('\n').each { it ->
			def temp = it.trim()
			if ( temp ) {
				stdout.add(it.trim())
			}
		}
		def stderr = []
		proc.err.text.split('\n').each { it ->
			def temp = it.trim()
			if ( temp ) {
				stderr.add(temp)
			}
			setStdOut(stdout)
			setStdErr(stderr)
		}

		log.debug("Executed: "+command())
	}

	@Override
	public String toString() {
		return 'command: "'+command()+'", stdout: "'+stdOut+'", stderr: "'+stdErr+'", exitCode: "'+exitCode+'"'
	}

	public void verify() {

		if ( getStdErr().size() > 0  ) {
			throw new GoldCommandException("Gold command failed.", this);
		}
	}
}
