package nz.org.nesi.goldwrap;

import java.io.File;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

	static final Logger myLogger = LoggerFactory.getLogger(Config.class);

	private static HierarchicalINIConfiguration config = null;

	public static File getConfigDir() {

		File dir = new File(System.getProperty("user.home"), ".goldwrap");
		if (dir.exists()) {
			return dir;
		}
		File globalDir = new File("/etc/goldwrap");
		if (globalDir.exists()) {
			return globalDir;
		}

		return dir;

	}

	/**
	 * Retrieves the configuration parameters from the properties file.
	 * 
	 * @return the configuration
	 * @throws ConfigurationException
	 *             if the file could not be read/parsed
	 */
	public static HierarchicalINIConfiguration getServerConfiguration()
			throws ConfigurationException {
		if (config == null) {
			final File grisuDir = getConfigDir();
			config = new HierarchicalINIConfiguration(new File(grisuDir,
					"goldwrap.config"));
			config.setReloadingStrategy(new FileChangedReloadingStrategy());
		}
		return config;
	}

	public static boolean useSSH() {
		boolean verify = false;

		try {
			try {
				verify = getServerConfiguration().getBoolean("useSSH");
			} catch (final NoSuchElementException e) {
				// doesn't matter
				// myLogger.debug(e.getLocalizedMessage(), e);
			}

		} catch (final ConfigurationException e) {
			myLogger.debug(e.getLocalizedMessage());
		}
		return verify;

	}

}
