package at.ac.tuwien.lsdc.actions;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;
import at.ac.tuwien.lsdc.Configuration;
import at.ac.tuwien.lsdc.generator.RequestGenerator;
import at.ac.tuwien.lsdc.mape.Monitor;
import at.ac.tuwien.lsdc.resources.App;
import at.ac.tuwien.lsdc.resources.PhysicalMachine;
import at.ac.tuwien.lsdc.resources.Resource;
import at.ac.tuwien.lsdc.resources.VirtualMachine;

public class ChangeVmConfiguration extends Action {
	private static Instances knowledgeBase = null;
	private static Classifier classifier = null;
	private static Evaluation evaluation = null;
	private static RandomData randomData = new RandomDataImpl();
	
	private App app;
	private VirtualMachine vm;
	private static int vmStartupCosts = 10;
	private static int pmStartupCosts = 20;	
	private static int vmChangeConfigurationCosts = 20;

	
	private int newCpuConfiguration;
	private int newMemorayConfiguration;
	private int newStorageConfiguration;
	
	private PhysicalMachine selectedPm = null;
	private boolean preconditionsOk = false;
	private int costs = 0;
	private int waitForEvaluation = 10;
	private Instance curInstance;
	private int prediction;
	
	
	public static Instances getKnowledgeBase() {
		if (knowledgeBase ==null ) {
			try {
				//load knowledgebase from file
				ChangeVmConfiguration.knowledgeBase = Action.loadKnowledge(Configuration.getInstance().getKBCreateVmInsertApp());
				
				//prediction is also performed therefore the classifier and the evaluator must be instantiated
				if(!isOnlyLearning()) {
					classifier = new MultilayerPerceptron();
					classifier.buildClassifier(ChangeVmConfiguration.getKnowledgeBase());
					evaluation = new Evaluation(ChangeVmConfiguration.getKnowledgeBase());
					evaluation.crossValidateModel(classifier, knowledgeBase, 10, knowledgeBase.getRandomNumberGenerator(randomData.nextLong(1, 1000)));
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return knowledgeBase;
	}
	
	public void terminate() {
		try {
			Action.saveKnowledge(Configuration.getInstance().getKBCreateVmInsertApp(), ChangeVmConfiguration.getKnowledgeBase());
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	@Override
	public int predict() {
		int output = 0;
		if (isOnlyLearning()) { //Randomized predictions for learning
			return randomData.nextInt(0, 100);
		}
		else { //Use WEKA - evaluation for prediction
			//Create new WEKA - instance
			return prediction; 
			
		}			
		
	}

	@Override
	public int estimate() {
		return costs;
	}

	@Override
	public boolean preconditions() {
		return preconditionsOk;
	}
	
	@Override
	public boolean evaluate() {
		if (curInstance == null) {
			curInstance = createInstance(0,vm); // create a Instance with the past values
		}
		
		if (app.getSuspendedTicks()>0 || app.getVm().getSuspendedTicks()>0 || app.getVm().getPm().getSuspendedTicks()>0) {
			return false;
		}
		else if (waitForEvaluation>0) {
			waitForEvaluation--;
			return false;
		}
		else {
			System.out.println("APP - Running Ticks: " + app.getRunningTicks());
			LinkedList<Integer> cpuusagehist =  vm.getCpuUsageHistory(10);	
			
			LinkedList<Integer> memusagehist = vm.getMemoryUsageHistory(10);	
				
			LinkedList<Integer> storageusagehist = vm.getStorageUsageHistory(10);	

			double evaluation =(255- calculateUsageRatio(cpuusagehist, 85) - calculateUsageRatio(memusagehist, 85) - calculateUsageRatio(storageusagehist, 85))/255 ;
			
			//subtract SLA Violations
			evaluation -= (app.getCpuSlaErrorcount()+app.getMemorySlaErrorcount()+app.getStorageSlaErrorcount())/10;
			
			//minimum of 0
			evaluation = Math.max(0, evaluation);
			Monitor.getInstance().logExecution(vm, this, evaluation, this.globalTickExecution);
			curInstance.setValue(getKnowledgeBase().attribute(63), evaluation);
			this.setLocalEvaluation(evaluation);
			getKnowledgeBase().add(curInstance);
		}
		return true;
	}
	
	private int clusterValue (int value) {
		return (int)(Math.ceil(value/10)*10);
	}
	
	private double calculateUsageRatio(LinkedList<Integer> usage, int goal) {
		double result = 0;
		for(int i=0; i<usage.size();i++){ 
			result += Math.abs(85-usage.get(i))/usage.size();
		}
		return result;
	}
	
	private void createConfiguration(VirtualMachine vm){
		
		newCpuConfiguration=vm.getCurrentCpuAllocation()-vm.getCurrentCpuUsage();
		newMemorayConfiguration=vm.getCurrentMemoryAllocation()-vm.getCurrentMemoryUsage();
		newStorageConfiguration=vm.getCurrentStorageAllocation()-vm.getCurrentStorageUsage(); 
				
	}

	//calculate if an App fits to a pm
	//TODO: gst: use WEKA to calc fit factor!!
	private int calculateFit(App app2, PhysicalMachine pm) {
		int output = 0;
		if (Action.isOnlyLearning()== false ){
			//is space available
			if (app2.getCpu()< (100-pm.getCurrentCpuAllocation()) && app2.getMemory() < (100-pm.getCurrentMemoryAllocation()) && app2.getStorage() < (100-pm.getCurrentCpuAllocation())) {		
				Instance instance = createInstance(Instance.missingValue(), vm);
				instance.setDataset(CreateAppInsertIntoVm.getKnowledgeBase());
				
				try {
					output = (int) (evaluation.evaluateModelOnce(classifier, instance) *100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			if (app2.getCpu()< (100-pm.getCurrentCpuAllocation()) && app2.getMemory() < (100-pm.getCurrentMemoryAllocation()) && app2.getStorage() < (100-pm.getCurrentCpuAllocation())) {
				return randomData.nextInt(1, 100);
			}
		}
		return output;
	}

	@Override
	public void execute() {
		globalTickExecution = Monitor.getInstance().getGlobalTicks();
		VirtualMachine oldVm = null;
		
		//if the App existed before
		if (app.getVm()!=null) {
			oldVm = app.getVm();
		}
		
		//remove the app from the request- queue??? 
		if(app.getOriginalRequest()!=null) {
			RequestGenerator.getInstance().removeRequestFromQueue(app.getOriginalRequest());
		}
		
		if (selectedPm.isRunning()==false) {
			selectedPm.startMachine();
		}
		
		//TODO: gst: hard - coded startup value!!
		VirtualMachine vm = selectedPm.createNewVm(app.getCpu(), app.getMemory(), app.getStorage(), 10);
		vm.createApp(this.app);
		
		if(oldVm!=null) {
			oldVm.getApps().remove(app);
			//TODO: gst: what happens if the VM is now empty??
			if(oldVm.getApps().size()==0) {
				oldVm.terminate();
			}
		}
	}

	@Override
	public void init(Resource problemVm) {
		this.preconditionsOk=false;
		this.selectedPm=null;
		this.costs=0;
		this.app=null;
		this.setProblemResource(problemVm);
		this.setProblemType(problemVm.getProblemType());
		problemVm.setProblemType("");
		
		ChangeVmConfiguration.vmStartupCosts = Configuration.getInstance().getVmStartupCosts();
		ChangeVmConfiguration.pmStartupCosts = Configuration.getInstance().getPmStartupCosts();
		ChangeVmConfiguration.vmChangeConfigurationCosts = Configuration.getInstance().getPmStartupCosts();
		
		
		if (problemVm instanceof VirtualMachine){
			vm = (VirtualMachine)problemVm;
			boolean found = false;
			int fitFactor = 0;
			int curFit = 0;
			
			if((100 - vm.getPm().getCurrentCpuAllocation())>=newCpuConfiguration &&(100 - vm.getPm().getCurrentMemoryAllocation())>=newMemorayConfiguration&&(100 - vm.getPm().getCurrentStorageAllocation())>=newStorageConfiguration){
				preconditionsOk = true;
				costs += pmStartupCosts;
				selectedPm = vm.getPm();
				prediction  =calculateFit(app, vm.getPm());
			}
			
	}
	
	}
	private Instance createInstance(double eval, VirtualMachine vm) {
		Instance instance = new Instance(64);
		
		LinkedList<Integer> cpuallhist = vm.getCpuAllocationHistory(10);
		LinkedList<Integer> cpuusehist = vm.getCpuUsageHistory(10);

		LinkedList<Integer> memallhist = vm.getMemoryAllocationHistory(10);
		LinkedList<Integer> memusehist = vm.getMemoryUsageHistory(10);
		
			
		LinkedList<Integer> storageallhist = vm.getStorageAllocationHistory(10);
		LinkedList<Integer> storageusehist = vm.getStorageUsageHistory(10);
		
		//CPU/Memory/Storage - Allocation history before the new vm was created
		for (int i = 0; i<10;i++) {
				//cpu allocation
				instance.setValue(getKnowledgeBase().attribute(i), clusterValue(cpuallhist.get(i)));
				//cpu usage
				instance.setValue(getKnowledgeBase().attribute(i+10), clusterValue(cpuusehist.get(i)));
				
				//memory allocation
				instance.setValue(getKnowledgeBase().attribute(i+20),clusterValue( memallhist.get(i)));
				//memory usage
				instance.setValue(getKnowledgeBase().attribute(i+30),clusterValue( memusehist.get(i)));
				
				//storage allocation
				instance.setValue(getKnowledgeBase().attribute(i+40), clusterValue(storageallhist.get(i)));
				//storage usage 
				instance.setValue(getKnowledgeBase().attribute(i+50),clusterValue( storageusehist.get(i)));
		}
		
		//SLAs
		//CPU
		instance.setValue(getKnowledgeBase().attribute(60), clusterValue(app.getCpu()));
		//Memory
		instance.setValue(getKnowledgeBase().attribute(61),clusterValue(app.getMemory()));
		//Storage
		instance.setValue(getKnowledgeBase().attribute(62), clusterValue(app.getStorage()));
		
		//Evaluation
		instance.setValue(getKnowledgeBase().attribute(63), eval);

		return instance;
	}

}