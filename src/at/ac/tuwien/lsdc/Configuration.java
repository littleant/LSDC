package at.ac.tuwien.lsdc;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
	private static volatile Configuration instance;
	
	private Integer topRegion = null;
	private Integer bottomRegion = null;
	
	private Integer maxPms = null; 
	
	private Integer pmStartupCosts = null;
	private Integer vmStartupCosts = null;
	
	public Integer getTopRegion() {
		return topRegion;
	}

	public void setTopRegion(Integer topRegion) {
		this.topRegion = topRegion;
	}

	public Integer getBottomRegion() {
		return bottomRegion;
	}

	public void setBottomRegion(Integer bottomRegion) {
		this.bottomRegion = bottomRegion;
	}

	public Integer getMaxPms() {
		return maxPms;
	}

	public void setMaxPms(Integer maxPms) {
		this.maxPms = maxPms;
	}
	
	private Configuration() {
		// read lsdc.properties file
		Properties properties = new Properties();
		BufferedInputStream stream;
		try {
			stream = new BufferedInputStream(new FileInputStream("lsdc.properties"));
			properties.load(stream);
			stream.close();
			
			this.topRegion = Integer.parseInt(properties.getProperty("topRegion"));
			this.bottomRegion = Integer.parseInt(properties.getProperty("bottomRegion"));
			this.maxPms = Integer.parseInt(properties.getProperty("maxPms"));
			this.pmStartupCosts = Integer.parseInt(properties.getProperty("pmStartupCosts"));
			this.vmStartupCosts = Integer.parseInt(properties.getProperty("vmStartupCosts"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Integer getPmStartupCosts() {
		return pmStartupCosts;
	}

	public void setPmStartupCosts(Integer pmStartupCosts) {
		this.pmStartupCosts = pmStartupCosts;
	}

	public Integer getVmStartupCosts() {
		return vmStartupCosts;
	}

	public void setVmStartupCosts(Integer vmStartupCosts) {
		this.vmStartupCosts = vmStartupCosts;
	}

	public static Configuration getInstance() {
		if (Configuration.instance == null) {
			synchronized (Configuration.class) {
				if (Configuration.instance == null) {
					Configuration.instance = new Configuration();
				}
			}
		}
		
		return Configuration.instance;
	}
}
