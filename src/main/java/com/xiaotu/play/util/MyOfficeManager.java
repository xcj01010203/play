package com.xiaotu.play.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

public class MyOfficeManager {

	private MyOfficeManager() {}
	
	private static OfficeManager officeManager = null;
	
	public static synchronized OfficeManager getInstance(String officeHome) throws FileNotFoundException, IOException {
		if (officeManager == null) {
			DefaultOfficeManagerConfiguration config = new DefaultOfficeManagerConfiguration();
			config.setOfficeHome(officeHome);
			config.setPortNumber(12345);
			
			officeManager = config.buildOfficeManager();
			officeManager.start();
		}
		return officeManager;
	}	
}
