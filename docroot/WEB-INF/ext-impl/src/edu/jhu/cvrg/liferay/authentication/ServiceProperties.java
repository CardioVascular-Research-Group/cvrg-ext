package edu.jhu.cvrg.liferay.authentication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ServiceProperties {
	
	private static String PROPERTIES_PATH = "/conf/service.properties";
	private static Properties prop;
	private static ServiceProperties singleton;
	private static File propertiesFile = null;
	private static long lastChange = 0;
	

	private ServiceProperties() {
		prop = new Properties();
		propertiesFile = new File(System.getProperty("catalina.home") + PROPERTIES_PATH);
		loadProperties();
	}
	
	public static ServiceProperties getInstance(){
		if(singleton == null){
			singleton = new ServiceProperties();
		}
		return singleton;
	}
	
	public String getProperty(String propertyName){
		loadProperties();
		return prop.getProperty(propertyName);
	}
	
	private void loadProperties(){
		try {
			if(propertiesFile.lastModified() > lastChange){
				prop.clear();
				prop.load(new FileReader(propertiesFile));
				lastChange = propertiesFile.lastModified();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
