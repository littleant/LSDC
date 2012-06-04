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
	private Double factorCostsEvaluation = null;
	
	private String KBCreateVmInsertApp = null;
	private String KBCreateAppInsertIntoVm = null;
	private String KBMoveVm = null;
	private String KBMaster =null;
	
	private int factorUsageEvaluation;
	private int factorSlaViolations;
	
	private boolean actionOnlyLearning = false;
	private boolean plannerOnlyLearning = false;

	private int appInsertIntoVmCosts;

	private int appMovingCosts;
	

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
			this.setAppMovingCosts(Integer.parseInt(properties.getProperty("appMovingCosts")));
			this.setAppInsertIntoVmCosts(Integer.parseInt(properties.getProperty("appInsertIntoVmCosts")));
			this.setActionOnlyLearning(Boolean.parseBoolean(properties.getProperty(" actionOnlyLearning")));
			this.setPlannerOnlyLearning(Boolean.parseBoolean(properties.getProperty(" actionOnlyLearning")));
			
			//evaluation factors for global evaluation
			this.setFactorSlaViolations(Integer.parseInt(properties.getProperty("factorSlaViolations")));
			this.setFactorUsageEvaluation(Integer.parseInt(properties.getProperty("factorUsageEvaluation")));
			this.setFactorCostsEvaluation(Double.parseDouble(properties.getProperty("factorCostsEvaluation")));
			//knowledge
			this.KBCreateVmInsertApp = properties.getProperty("KBCreateVmInsertApp");
			this.KBCreateAppInsertIntoVm = properties.getProperty("KBCreateAppInsertIntoVm");
			this.KBMoveVm = properties.getProperty("KBMoveVm");
			this.setKBMaster(properties.getProperty("KBMaster"));
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

	public String getKBCreateVmInsertApp() {
		return KBCreateVmInsertApp;
	}

	public void setKBCreateVmInsertApp(String kBCreateVmInsertApp) {
		KBCreateVmInsertApp = kBCreateVmInsertApp;
	}

	public String getKBCreateAppInsertIntoVm() {
		return KBCreateAppInsertIntoVm;
	}

	public void setKBCreateAppInsertIntoVm(String kBCreateAppInsertIntoVm) {
		KBCreateAppInsertIntoVm = kBCreateAppInsertIntoVm;
	}

	public int getAppInsertIntoVmCosts() {
		return appInsertIntoVmCosts;
	}

	public void setAppInsertIntoVmCosts(int appInsertIntoVmCosts) {
		this.appInsertIntoVmCosts = appInsertIntoVmCosts;
	}

	public int getAppMovingCosts() {
		return appMovingCosts;
	}

	public void setAppMovingCosts(int appMovingCosts) {
		this.appMovingCosts = appMovingCosts;
	}

	public String getKBMoveVm() {
		return KBMoveVm;
	}

	public void setKBMoveVm(String kBMoveVm) {
		KBMoveVm = kBMoveVm;
	}

	public String getKBMaster() {
		return KBMaster;
	}

	public void setKBMaster(String kBMaster) {
		KBMaster = kBMaster;
	}

	public int getFactorUsageEvaluation() {
		return factorUsageEvaluation;
	}

	public void setFactorUsageEvaluation(int factorUsageEvaluation) {
		this.factorUsageEvaluation = factorUsageEvaluation;
	}

	public int getFactorSlaViolations() {
		return factorSlaViolations;
	}

	public void setFactorSlaViolations(int factorSlaViolations) {
		this.factorSlaViolations = factorSlaViolations;
	}

	public Double getFactorCostsEvaluation() {
		return factorCostsEvaluation;
	}

	public void setFactorCostsEvaluation(Double factorCostsEvaluation) {
		this.factorCostsEvaluation = factorCostsEvaluation;
	}

	public boolean isActionOnlyLearning() {
		return actionOnlyLearning;
	}

	public void setActionOnlyLearning(boolean actionOnlyLearning) {
		this.actionOnlyLearning = actionOnlyLearning;
	}

	public boolean isPlannerOnlyLearning() {
		return plannerOnlyLearning;
	}

	public void setPlannerOnlyLearning(boolean plannerOnlyLearning) {
		this.plannerOnlyLearning = plannerOnlyLearning;
	}
}
