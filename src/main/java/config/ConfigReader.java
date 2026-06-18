package config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

	static Properties properties;

	public ConfigReader() {
		try {
			FileInputStream fs = new FileInputStream("src/test/resources/config.properties");
			properties = new Properties();
			try {
				properties.load(fs);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static String getKeyValue(String key) {
		// System property (-D flag from Jenkins/Maven) takes priority over config.properties
		String systemValue = System.getProperty(key);
		if (systemValue != null) {
			return systemValue;
		}
		return properties.getProperty(key);
	}
}
