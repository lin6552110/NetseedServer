package com.switek.netseed.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Settings {

	static Logger logger = Logger.getLogger(Settings.class);

	public Settings() {
		// TODO Auto-generated constructor stub
	}

	static Properties prop = null;

	private static void init() {
		try {
			InputStream in = new FileInputStream(new File("system.properties"));
			prop = new Properties();
			prop.load(in);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public static String getSetting(String key) {
		if (prop == null) {
			init();
		}

		return prop.get(key).toString();
	}

	public static String getAppName() {
		return "Netseed Server";

	}

}
