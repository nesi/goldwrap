package nz.org.nesi.goldwrap.util

import groovy.util.logging.Slf4j
import nz.org.nesi.goldwrap.Config
import nz.org.nesi.goldwrap.domain.ExternalCommand
import nz.org.nesi.goldwrap.errors.UserFault

import org.apache.commons.lang3.StringUtils

import com.google.common.base.Joiner
import com.google.common.base.Splitter
import com.google.common.collect.Lists

@Slf4j
class GoldWrap {

	public static void createUser(String username, String fullName, String email, String phone) {

		if ( StringUtils.isBlank(username) ) {
			throw new UserFault("Can't create user.", "Username not specified", 400)
		}

		if ( StringUtils.isBlank(fullName) ) {
			throw new UserFault("Can't create user.", "Full name not specified", 400)
		}

		if ( StringUtils.isBlank(email) ) {
			throw new UserFault("Can't create user.", "Email address specified", 400)
		}


		if (isRegistered(username)) {
			throw new UserFault("Can't create user.", "User " + username
			+ " already in Gold database.", 409)
		}


		List<String> command = Lists.newArrayList("gmkuser")
		if (StringUtils.isNotBlank(fullName)) {
			command.add("-n")
			command.add(fullName)
		}
		if (StringUtils.isNotBlank(email)) {
			command.add("-E")
			command.add(email)
		}
		if (StringUtils.isNotBlank(phone)) {
			command.add("-F")
			command.add(phone)
		}

		command.add(username)

		ExternalCommand ec = executeGoldCommand(command)

		if (!GoldWrap.isRegistered(username)) {
			throw new UserFault("Can't create user "+username+".", "Unknown reason", 500)
		}
	}

	public static void deleteUser(String username) {

		ExternalCommand ec = executeGoldCommand("grmuser "+username)

		if ( isRegistered(username) ) {
			throw new UserFault("Could not delete "+username+".", "Unknown reason")
		}

	}

	public static void modifyUser(String username, String fullName, String email, String phone) {

		if (StringUtils.isBlank(username)) {
			throw new UserFault("Can't modify user.",
			"Username not specified.")
		}

		if (!isRegistered(username)) {
			throw new UserFault("Can't modify user.", "User " + username
			+ " not in Gold database.", 404)
		}


		List<String> command = Lists.newArrayList("gchuser")
		if (StringUtils.isNotBlank(fullName)) {
			command.add("-n")
			command.add("fullname")
		}
		if (StringUtils.isNotBlank(email)) {
			command.add("-E")
			command.add(email)
		}
		if (StringUtils.isNotBlank(phone)) {
			command.add("-F")
			command.add(phone)
		}

		command.add(username)

		ExternalCommand ec = executeGoldCommand(command)


	}











	private static ExternalCommand executeGoldCommand(List<String> command) {
		ExternalCommand gc = new ExternalCommand(command)
		execute(gc)
		gc.verify()
		return gc
	}

	private static ExternalCommand executeGoldCommand(String command) {
		def list = command.tokenize()
		return executeGoldCommand(list)
	}


	static boolean isRegistered(String username) {

		ExternalCommand gc = executeGoldCommand("glsuser -show Name -quiet")

		if (gc.getStdOut().contains(username)) {
			return true
		} else {
			return false
		}
	}


	private static void execute(ExternalCommand ec) {

				if ( ec.getExecuted() ) {
					throw new RuntimeException("Command already executed.")
				}

				ec.setExecuted(new Date())
				Process proc = null

					log.debug("Executing command..."+ec.toString())
					List<String> commandToExecute = []
					// checking whether we can use gold directly
					if ( Config.getCommandPrefix() ) {
						Iterable config = Splitter.on(' ').split(Config.getCommandPrefix())
						config = Lists.newArrayList(config)
						def last = config.last()
						config.pop()
						def first = ec.getCommand().first()
						def joined = last+first
						ec.getCommand().remove(0)

						commandToExecute = [
							config,
							joined,
							ec.getCommand()
						].flatten()

					} else {
						commandToExecute = ec.getCommand()
					}
					if ( commandToExecute[0] == 'ssh' ) {
						log.debug("Escaping special characters because of ssh...")
						def temp = []
						for ( String token : commandToExecute ) {
							if ( ! token ) {
								continue
							}
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
						commandToExecute = temp
					}


					log.debug('\n\n'+Joiner.on('\n').join(commandToExecute.iterator())+'\n\n')
					ProcessBuilder procBuilder = new ProcessBuilder(commandToExecute)
					proc = procBuilder.start()

				proc.waitFor()
				ec.setFinished(new Date())

				ec.setExitCode(proc.exitValue())
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
					ec.setStdOut(stdout)
					ec.setStdErr(stderr)
				}

				if ( Config.debugEnabled() ) {
					log.debug("STDOUT:\n\n"+Joiner.on('\n').join(stdout.iterator()))
					log.debug("\nSTDERR:\n\n"+Joiner.on('\n').join(stderr.iterator()))
				}

				log.debug("Executed advanced command: "+ec.toString())

			}
}
