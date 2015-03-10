package com.switek.netseed.server;

import com.switek.netseed.server.ui.ImportIRCodeForm;
import com.switek.netseed.server.ui.ServerForm;

public class Main {

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			String arg = "";
			if (args.length > 0) {
				arg = args[0].toLowerCase();
			}

			if (arg.length() == 0 || arg.equals("server")) {
				ServerForm window = new ServerForm();
				window.open();
			} else if (arg.equals("importircode")) {
				ImportIRCodeForm form = new ImportIRCodeForm();
				form.open();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
