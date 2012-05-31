package at.ac.tuwien.lsdc.actions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;
import at.ac.tuwien.lsdc.Configuration;
import at.ac.tuwien.lsdc.mape.Monitor;
import at.ac.tuwien.lsdc.resources.PhysicalMachine;
import at.ac.tuwien.lsdc.resources.Resource;
import at.ac.tuwien.lsdc.resources.VirtualMachine;

/**
 * Action to move a VM to a different PM
 */
public class MoveVm extends Action {
	private static Instances knowledgeBase = null;
	private static Classifier classifier = null;
	private static Evaluation evaluation = null;
	private static RandomData randomData = new RandomDataImpl();
	
	private VirtualMachine vm;
	private boolean preconditionsOk = false;
	private int costs = 0;
	private PhysicalMachine selectedPm = null;
	private static int vmStartupCosts = 10;
	
	private int waitForEvaluation = 0;

	public static Instances getKnowledgeBase() {
		if (knowledgeBase == null) {
			try {
				//load knowledgebase from file
				MoveVm.knowledgeBase = Action.loadKnowledge(Configuration.getInstance().getKBMoveVm());
				
				//prediction is also performed therefore the classifier and the evaluator must be instantiated
				if(!isOnlyLearning()) {
					classifier = new MultilayerPerceptron();
					classifier.buildClassifier(MoveVm.getKnowledgeBase());
					evaluation = new Evaluation(MoveVm.getKnowledgeBase());
					evaluation.crossValidateModel(classifier, knowledgeBase, 10, knowledgeBase.getRandomNumberGenerator(randomData.nextLong(1, 1000)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return knowledgeBase;
	}
	
	@Override
	public void init(Resource problemVm) {
		this.preconditionsOk = false;
		this.selectedPm = null;
		this.costs = 0;
		this.vm = null;
		
		MoveVm.vmStartupCosts = Configuration.getInstance().getVmStartupCosts();
		
		if (problemVm instanceof VirtualMachine){
			this.vm = (VirtualMachine) problemVm;
			boolean found = false;
			int fitFactor = 0;
			
			for (PhysicalMachine pm : Monitor.getInstance().getPms()) {
				if (pm.isRunning())  {
					if ((100 - pm.getCurrentCpuAllocation()) >= vm.getCurrentCpuAllocation() && (100 - pm.getCurrentMemoryAllocation()) >= vm.getCurrentMemoryAllocation() && (100 - pm.getCurrentCpuAllocation()) >= vm.getCurrentStorageAllocation()) {
						found = true;
						this.costs = vmStartupCosts;
						
						if (this.selectedPm == null || this.calculateFit(this.vm, pm) > fitFactor) {
							this.selectedPm = pm;
						}
					}
				}
			}
			
			preconditionsOk = found;
		}
		else {
			preconditionsOk= false;
		}
	}
	
	/**
	 * Calculate a fit value: How good does the VM fit into the PM?
	 * 
	 * @param vm VM that should be transfered
	 * @param pm PM that should be calculated the fit value against
	 * @return Fit value
	 */
	private int calculateFit(VirtualMachine vm, PhysicalMachine pm) {
		int cpuFit = pm.getCurrentCpuAllocation() - vm.getCurrentCpuAllocation();
		int memFit = pm.getCurrentMemoryAllocation() - vm.getCurrentMemoryAllocation();
		int storageFit = pm.getCurrentStorageAllocation() - vm.getCurrentStorageAllocation();
		
		return (100 - cpuFit) + (100 - memFit) + (100 - storageFit);
	}

	@Override
	public int predict() {
		int output = 0;
		
		if (isOnlyLearning()) { // Randomized predictions for learning
			return randomData.nextInt(0, 100);
		} else { // Use WEKA - evaluation for prediction
			// Create new WEKA - instance
			Instance instance = new Instance(61);

			// PM history
			LinkedList<Integer> pmCpuHistory = this.selectedPm.getCpuAllocationHistory(10);
			LinkedList<Integer> pmMemoryHistory = this.selectedPm.getMemoryAllocationHistory(10);
			LinkedList<Integer> pmStorageHistory = this.selectedPm.getStorageAllocationHistory(10);
			
			// VM history
			List<Integer> vmCpuHistory = this.vm.getCpuAllocationHistory(10);
			List<Integer> vmMemoryHistory = this.vm.getMemoryAllocationHistory(10);
			List<Integer> vmStorageHistory = this.vm.getStorageAllocationHistory(10);
			
			// CPU/Memory/Storage - Allocation history before the VM is moved
			for (int i = 0; i < 10; i++) {
				// VM history
				instance.setValue(MoveVm.getKnowledgeBase().attribute(i), clusterValue(vmCpuHistory.get(i)));
				instance.setValue(MoveVm.getKnowledgeBase().attribute(i + 10), clusterValue(vmMemoryHistory.get(i)));
				instance.setValue(MoveVm.getKnowledgeBase().attribute(i + 20), clusterValue(vmStorageHistory.get(i)));
				
				// PM history
				instance.setValue(getKnowledgeBase().attribute(i + 30), clusterValue(pmCpuHistory.get(i)));
				instance.setValue(getKnowledgeBase().attribute(i + 40), clusterValue(pmMemoryHistory.get(i)));
				instance.setValue(getKnowledgeBase().attribute(i + 50), clusterValue(pmStorageHistory.get(i)));
			}
			
			//Evaluation
			instance.setValue(getKnowledgeBase().attribute(60), Instance.missingValue());
	
			instance.setDataset(CreateVmInsertApp.getKnowledgeBase());
			
			try {
				output = (int) (evaluation.evaluateModelOnce(classifier, instance));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return output;
	}

	private static int clusterValue(int value) {
		return (int) (Math.ceil(value / 10) * 10);
	}
	
	@Override
	public int estimate() {
		return this.costs;
	}

	@Override
	public boolean preconditions() {
		return this.preconditionsOk;
	}

	@Override
	public void execute() {
		// remove VM from old PM
		VirtualMachine vm = this.vm;
		this.vm.getPm().getVms().remove(this.vm);
		
		// insert VM into new PM
		if (this.preconditions()) {
			this.selectedPm.getVms().add(vm);
		}
	}

	@Override
	public boolean evaluate() {
		if (this.vm.getPm().getSuspendedTicks() > 0 || this.vm.getSuspendedTicks() > 0) {
			return false;
		} else if (waitForEvaluation > 0) {
			waitForEvaluation--;
			
			return false;
		} else {
			// PM history
			List<Integer> pmCpuUsageHistory = this.selectedPm.getCpuUsageHistory(10);
			List<Integer> pmMemoryUsageHistory = this.selectedPm.getMemoryUsageHistory(10);
			List<Integer> pmStorageUsageHistory = this.selectedPm.getStorageUsageHistory(10);
			
			// VM history
			List<Integer> vmCpuUsageHistory = this.vm.getCpuUsageHistory(10);
			List<Integer> vmMemoryUsageHistory = this.vm.getMemoryUsageHistory(10);
			List<Integer> vmStorageUsageHistory = this.vm.getStorageUsageHistory(10);	
			
			// evaluate usage 
			// (510-(abs(85-pmcpu)-abs(85-pmmem)-abs(85-pmstor)-abs(85-vmcpu)-abs(85-vmmem)-abs(85-vmstor)))/510
			double evaluation = (510 - calculateUsageRatio(pmCpuUsageHistory, 85) - calculateUsageRatio(pmMemoryUsageHistory, 85) - calculateUsageRatio(pmStorageUsageHistory, 85) - calculateUsageRatio(vmCpuUsageHistory, 85) - calculateUsageRatio(vmMemoryUsageHistory, 85) - calculateUsageRatio(vmStorageUsageHistory, 85)) / 510;
			
			// minimum of 0
			//evaluation = Math.max(0, evaluation);
			
			// create new WEKA - instance
			Instance instance = new Instance(61);
			
			/// PM history
			List<Integer> pmCpuHistory = this.selectedPm.getCpuAllocationHistory(10);
			List<Integer> pmMemoryHistory = this.selectedPm.getMemoryAllocationHistory(10);
			List<Integer> pmStorageHistory = this.selectedPm.getStorageAllocationHistory(10);
			
			// VM history
			List<Integer> vmCpuHistory = this.vm.getCpuAllocationHistory(10);
			List<Integer> vmMemoryHistory = this.vm.getMemoryAllocationHistory(10);
			List<Integer> vmStorageHistory = this.vm.getStorageAllocationHistory(10);
			
			// CPU/Memory/Storage - Allocation history before the VM is moved
			for (int i = 0; i < 10; i++) {
				// VM history
				instance.setValue(MoveVm.getKnowledgeBase().attribute(i), clusterValue(vmCpuHistory.get(i)));
				instance.setValue(MoveVm.getKnowledgeBase().attribute(i + 10), clusterValue(vmMemoryHistory.get(i)));
				instance.setValue(MoveVm.getKnowledgeBase().attribute(i + 20), clusterValue(vmStorageHistory.get(i)));
				
				// PM history
				instance.setValue(getKnowledgeBase().attribute(i + 30), clusterValue(pmCpuHistory.get(i)));
				instance.setValue(getKnowledgeBase().attribute(i + 40), clusterValue(pmMemoryHistory.get(i)));
				instance.setValue(getKnowledgeBase().attribute(i + 50), clusterValue(pmStorageHistory.get(i)));
			}
			
			//Evaluation
			instance.setValue(getKnowledgeBase().attribute(33), evaluation);

			getKnowledgeBase().add(instance);
		}
		return true;
	}

	private static double calculateUsageRatio(List<Integer> usage, int goal) {
		double result = 0;
		for(int i=0; i<usage.size();i++){ 
			result += Math.abs(85-usage.get(i))/usage.size();
		}
		return result;
	}
	
	@Override
	public void terminate() {
		try {
			Action.saveKnowledge(Configuration.getInstance().getKBMoveVm(), MoveVm.getKnowledgeBase());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PhysicalMachine getSelectedPm() {
		return this.selectedPm;
	}
}
