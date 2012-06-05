package at.ac.tuwien.lsdc.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import at.ac.tuwien.lsdc.Configuration;
import at.ac.tuwien.lsdc.resources.App;
import at.ac.tuwien.lsdc.resources.PhysicalMachine;
import at.ac.tuwien.lsdc.resources.Resource;
import at.ac.tuwien.lsdc.resources.VirtualMachine;

public abstract class Action {
	
	private static boolean onlyLearning = false;
	
	private Resource problemResource;
	
	private double localEvaluation ;
	
	//Average Resource Usage Rate before the Action
	private int beforeResourceUsage = 0;
	
	//SLA Vioalations in a certain timeframe before the Action
	private int beforeSlaViolations = 0;
	
	//Average Resource Usage Rate after the Action
	private int afterResourceUsage = 0;
	
	//Average Resource Usage Rate after the Action
	private int afterSlaViolations = 0;
	
	protected long globalTickExecution=0;
	
	private String problemType ;
	
	/**
	 * Initialize the action
	 * 
	 * @param problem Problem the action should solve
	 */
	public abstract void init(Resource problem);
	
	/**
	 * Predict the outcome of the action if it would be used and try to calculate how good the action would perform
	 * 
	 * @return A value that evaluates the action
	 */
	public abstract int predict();
	
	/**
	 * Estimate the direct costs of the action
	 * 
	 * @return cost value
	 */
	public abstract int estimate();
	
	// are the preconditions fulfilled?
	public abstract boolean preconditions();
	
	// execute the action 
	public abstract void execute();
	
	// evaluate the action after the execution and add an entry to the knowledgebase
	public abstract boolean evaluate();
	
	/**
	 * used to save the knowledgebase
	 */
	public abstract void terminate();
	
	
	// read the knowledgebase to an arff - file
	public static Instances loadKnowledge(String filepath) throws Exception {
		File ftest = new File(filepath);
		Instances knowledgeBase = null;

		if (!ftest.exists()) {
			// create an empty ARFF and save it
			DataSource source = new DataSource(filepath);
			Instances data = source.getDataSet();
			if (data.classIndex() == -1) {
				data.setClassIndex(data.numAttributes() - 1);
			}
			
			ftest.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
			writer.write(data.toString());
			writer.newLine();
			writer.flush();
			writer.close();
		}
		
		if (ftest.exists()) {
			// load data
			DataSource source = new DataSource(filepath);
			 
			knowledgeBase = source.getDataSet();
	
			// setting class attribute if the data format does not provide this information
			// For example, the XRFF format saves the class attribute information as well
			if (knowledgeBase.classIndex() == -1){
				knowledgeBase.setClassIndex(knowledgeBase.numAttributes() - 1);
			}
		}
		 
		return knowledgeBase;
		 
		 
	}
	
	//saves the knowledgebase to an arff - file
	//TODO: gst: Problem bei Zur�ckschreiben in die gleiche Datei 
	public static void saveKnowledge(String filepath, Instances knowledgeBase) throws IOException {
		
		ArffSaver saver = new ArffSaver();
		
		saver.setInstances(knowledgeBase);
		File file = null;
		//Workaround for GST
		if (Configuration.getInstance().isDifferentOutputDirectory()) {
			file = new File("output"+filepath);
		}
		else {
			file = new File(filepath);
		}
		
		if(file.exists()) {
			System.out.println("deleting file..." + file.getAbsolutePath());
			System.out.println(file.delete());
			
			
		}
		System.out.println("creating new, empty file..." + file.getAbsolutePath());
		System.out.println(file.createNewFile());
		
		if(file.exists() && file.canWrite()) {
			System.out.println("Exists and can write");
		}
		else {
			System.out.println("Not exists or not writeable");
		}
		synchronized (System.out) {
			saver.setFile(file);
			System.out.println("save file..." + file.getAbsolutePath());
			saver.writeBatch();
			
		}
		
		
		
	}
	
	public static boolean isOnlyLearning() {
		return onlyLearning;
	}

	public static void setOnlyLearning(boolean onlyLearning) {
		Action.onlyLearning = onlyLearning;
	}

	public int getBeforeResourceUsage() {
		return beforeResourceUsage;
	}

	public void setBeforeResourceUsage(int beforeResourceUsage) {
		this.beforeResourceUsage = beforeResourceUsage;
	}

	public int getBeforeSlaViolations() {
		return beforeSlaViolations;
	}

	public void setBeforeSlaViolations(int beforeSlaViolations) {
		this.beforeSlaViolations = beforeSlaViolations;
	}

	public int getAfterResourceUsage() {
		return afterResourceUsage;
	}

	public void setAfterResourceUsage(int afterResourceUsage) {
		this.afterResourceUsage = afterResourceUsage;
	}

	public int getAfterSlaViolations() {
		return afterSlaViolations;
	}

	public void setAfterSlaViolations(int afterSlaViolations) {
		this.afterSlaViolations = afterSlaViolations;
	}

	public double getLocalEvaluation() {
		return localEvaluation;
	}

	public void setLocalEvaluation(double localEvaluation) {
		this.localEvaluation = localEvaluation;
	}

	public String getProblemType() {
		return problemType;
	}

	public void setProblemType(String problemType) {
		this.problemType = problemType;
	}
	
	public String getResourceType(Resource r){
		if (r instanceof PhysicalMachine) {
			return "PM";
		}
		else if (r instanceof VirtualMachine) {
			return "VM";
		}
		else if (r instanceof App) {
			return "App";
		}
		else {
			return "UnknownResource";
		}
	}

	public Resource getProblemResource() {
		return problemResource;
	}

	public void setProblemResource(Resource problemResource) {
		this.problemResource = problemResource;
	}
	

		
	
}
