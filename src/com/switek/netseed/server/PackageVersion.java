package com.switek.netseed.server;
/**
 * 
 */


import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author F1008570
 * 
 */
public class PackageVersion {

	public static String getVersion() {

		String ver = PackageVersion.class.getPackage()
				.getImplementationVersion();
		if (ver == null) {
			ver = getPackageVersion();
		}
		if (ver == null) {
			ver = "1.0.0";
		}
		return ver;
	}

	public static String getPackageVersion() {
		InputStream stream = Thread.class
				.getResourceAsStream("/META-INF/MANIFEST.MF");

		if (stream == null) {
			System.out.println("null stream");
			return "1.0.0";
		}

		Manifest manifest;
		try {
			manifest = new Manifest(stream);
		} catch (IOException e) {
			e.printStackTrace();
			return "1.0.0";
		}

		Attributes attributes = manifest.getMainAttributes();

		String impTitle = attributes.getValue("Implementation-Title");
		String impVersion = attributes.getValue("Implementation-Version");
		String impBuildDate = attributes.getValue("Built-Date");
		String impBuiltBy = attributes.getValue("Built-By");

		if (impTitle != null) {
			System.out.println("Implementation-Title:   " + impTitle);
		}
		if (impVersion != null) {
			System.out.println("Implementation-Version: " + impVersion);
		}
		if (impBuildDate != null) {
			System.out.println("Built-Date: " + impBuildDate);
		}
		if (impBuiltBy != null) {
			System.out.println("Built-By:   " + impBuiltBy);
		}

		return impVersion;
	}
}
