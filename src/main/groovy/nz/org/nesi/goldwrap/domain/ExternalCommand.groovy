package nz.org.nesi.goldwrap.domain


import groovy.util.logging.Slf4j

import javax.xml.bind.annotation.XmlRootElement

import nz.org.nesi.goldwrap.Config
import nz.org.nesi.goldwrap.errors.GoldCommandException

import com.google.common.base.Joiner
import com.google.common.base.Splitter
import com.google.common.collect.Lists



@XmlRootElement
@Slf4j
class ExternalCommand {

	static hasMany = [stdout : String, stderr : String]

	String command
	List<String> advancedCommand
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

	public ExternalCommand(List<String> advanced) {
		this.advancedCommand = advanced
	}

	public String command() {
		if ( Config.getCommandPrefix() ) {
			return Config.getCommandPrefix()+command
		} else {
			return command
		}
	}

	public String[] advancedCommand() {
		if ( Config.getCommandPrefix() ) {
			Iterable config = Splitter.on(' ').split(Config.getCommandPrefix())
			config = Lists.newArrayList(config)
			def last = config.last()
			config.pop()
			def first = advancedCommand.first()
			def joined = last+first
			advancedCommand.remove(0)

			def result = [
				config,
				joined,
				advancedCommand
			].flatten()
			return result
		} else {
			return advancedCommand
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

		setExecuted(new Date())
		Process proc = null
		if ( command ) {

			log.debug("Executing: "+command())

			proc = command().execute()
		} else {
			log.debug("Executing advanced command...")

			def ac = advancedCommand()

			if ( ac[0] == 'ssh' ) {
				log.debug("Escaping special characters because of ssh...")
				def temp = []
				for ( String token : ac ) {
					String tokenTemp = token.replace('"', '\\"')
//					tokenTemp = tokenTemp.replace('{', '\\{')
//					tokenTemp = tokenTemp.replace('}', '\\}')
					if ( token != tokenTemp ) {
						tokenTemp = '"'+tokenTemp+'"'
					} else {
						tokenTemp = tokenTemp.replace(' ', '\\ ')
					}
					log.debug("\t\tnew token: "+tokenTemp)
					temp.add(tokenTemp)
				}
				ac = temp
			}


			log.debug('\n\n'+Joiner.on('\n').join(ac.iterator())+'\n\n')
			ProcessBuilder procBuilder = new ProcessBuilder(ac)
			proc = procBuilder.start()
		}
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

		if ( Config.debugEnabled() ) {
			log.debug("STDOUT:\n\n"+Joiner.on('\n').join(stdout.iterator()))
			log.debug("\nSTDERR:\n\n"+Joiner.on('\n').join(stderr.iterator()))
		}

		if ( command ) {
			log.debug("Executed: "+command())
		} else {
			log.debug("Executed advanced command...")
		}
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
