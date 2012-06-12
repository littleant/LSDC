package at.ac.tuwien.lsdc.mape;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.instance.Resample;
import at.ac.tuwien.lsdc.Configuration;
import at.ac.tuwien.lsdc.actions.Action;
import at.ac.tuwien.lsdc.actions.ChangeVmConfiguration;
import at.ac.tuwien.lsdc.actions.CreateAppInsertIntoVm;
import at.ac.tuwien.lsdc.actions.CreateVmInsertApp;
import at.ac.tuwien.lsdc.actions.DoNothing;
import at.ac.tuwien.lsdc.actions.MoveApp;
import at.ac.tuwien.lsdc.actions.MoveVm;
import at.ac.tuwien.lsdc.actions.Outsource;
import at.ac.tuwien.lsdc.actions.TurnOffPmAndMoveVms;
import at.ac.tuwien.lsdc.resources.App;
import at.ac.tuwien.lsdc.resources.Resource;
import at.ac.tuwien.lsdc.resources.VirtualMachine;

public class WekaPlanner extends Planner {
	List<Class> knownActions = new LinkedList<Class>();
	
	private static boolean onlyLearning = false;
	private static Instances knowledgeBase = null;
	private static Classifier classifier = null;
	private static Evaluation evaluation = null;
	private static RandomData randomData = new RandomDataImpl();
	
	public static Instances getKnowledgeBase() {
		if (WekaPlanner.knowledgeBase ==null ) {
			try {
				//load knowledgebase from file
				WekaPlanner.knowledgeBase = Action.loadKnowledge(Configuration.getInstance().getKBMaster());
				
				//prediction is also performed therefore the classifier and the evaluator must be instantiated
				System.out.println ("WEKA - Panner is only learning: " + WekaPlanner.isOnlyLearning());
				if(WekaPlanner.isOnlyLearning()==false) {
					System.out.println("START up WEKA Classifier");
					Resample rs = new Resample();
					rs.setInputFormat(knowledgeBase); 
					rs.setSampleSizePercent(10);
					knowledgeBase = Resample.useFilter(knowledgeBase, rs);
					classifier = new MultilayerPerceptron();
					classifier.buildClassifier(WekaPlanner.knowledgeBase);
					
					evaluation = new Evaluation(WekaPlanner.knowledgeBase);
					evaluation.crossValidateModel(classifier, WekaPlanner.knowledgeBase, 10, knowledgeBase.getRandomNumberGenerator(randomData.nextLong(1, 1000)));
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return knowledgeBase;
	}
	
	public WekaPlanner() {
		Action.setOnlyLearning(Configuration.getInstance().isActionOnlyLearning());
		WekaPlanner.setOnlyLearning(Configuration.getInstance().isPlannerOnlyLearning());
		knownActions.add(CreateVmInsertApp.class);
		knownActions.add(CreateAppInsertIntoVm.class);
	    knownActions.add(MoveApp.class);
		knownActions.add(MoveVm.class);
		knownActions.add(DoNothing.class);
		//knownActions.add(Outsource.class);
		knownActions.add(TurnOffPmAndMoveVms.class);
		knownActions.add(ChangeVmConfiguration.class);
	}
	
	@Override
	public Action selectAction(Resource problem) {
		int pastResourceUsage = Monitor.getInstance().getGlobalAverageResourceUsageRate(10);
		int pastSlaViolations = Monitor.getInstance().getGlobalNumberOfSlaViolations(10);
		int currentFit=0;
		Action selectedAction = null;
		for (Class ac : knownActions) {
			try {
				GregorianCalendar gc = new GregorianCalendar();
				
				Action a = (Action)ac.newInstance();
				a.init(problem);
				a.setBeforeResourceUsage(pastResourceUsage);
				a.setBeforeSlaViolations(pastSlaViolations);
				int newFit = calculateFit(a);
				if (a.preconditions() && newFit>currentFit) {
					selectedAction= a;
					currentFit = newFit;
				}
				GregorianCalendar gc2 = new GregorianCalendar();
				
				// logging
				Monitor.getInstance().logPossibilities(problem, a, gc2.getTimeInMillis() - gc.getTimeInMillis());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return selectedAction;
	}
	
	public void evaluatePastActions () {
		
			LinkedList<Action>rmActionList = new LinkedList<Action>();
			//Knowledge aquisition
			for (Action a: executedActions) {
				if(a!=null) {
					boolean evaluated = a.evaluate();
					//System.out.println(evaluated  + " " + a.getProblemType() + " + " + a.getResourceType(a.getProblemResource()) +" => " + a.getClass().getSimpleName());
					if(evaluated) {
						//only evaluate if the predictions by the actions are not random!!
						if (Action.isOnlyLearning()==false ) {
							a.setAfterResourceUsage(Monitor.getInstance().getGlobalAverageResourceUsageRate(10));
							a.setAfterSlaViolations(Monitor.getInstance().getGlobalNumberOfSlaViolations(10));
						    
							int usageDiff = a.getAfterResourceUsage() - a.getBeforeResourceUsage();
							int slaDiff = a.getAfterSlaViolations() - a.getBeforeSlaViolations();
							int costs = a.estimate();
							double evaluationValue = Configuration.getInstance().getFactorUsageEvaluation()*usageDiff + Configuration.getInstance().getFactorSlaViolations()*slaDiff - Configuration.getInstance().getFactorCostsEvaluation()*costs;
										
							Instance instance = createInstance(a, evaluationValue);
							WekaPlanner.getKnowledgeBase().add(instance);
						}
						
						rmActionList.add(a); // the action has been evaluated and can be removed
					}
				}
			}

			for (Action a: rmActionList) {
				executedActions.remove(a); //remove actions that have been evaluated
			}
			
	
		
	}

	@Override
	public void terminate() {
		for (Class<Action> ac: knownActions) {
			try {
				//Master knowledge
				Action.saveKnowledge(Configuration.getInstance().getKBMaster(), WekaPlanner.getKnowledgeBase());
				
				//terminate Actions => save knowledge
				
				((Action)ac.newInstance()).terminate();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Instance createInstance (Action a, double evaluationValue){
	
		Instance instance = new Instance(6);
		
		instance.setDataset(getKnowledgeBase());
		Instances ist = getKnowledgeBase();
		String rt = "";
		Resource r = a.getProblemResource();
		if (r instanceof App){
			rt ="app";
		}
		else if (r instanceof VirtualMachine){
			rt ="vm";
		}
		else {
			rt ="pm";
		}
		
		instance.setValue(0, a.getProblemType());  //Problemtype
		instance.setValue(1, rt);  //ResourceType
		instance.setValue(2, a.getClass().getSimpleName()); //Actionname
		instance.setValue(3, a.estimate()); //estimation value
		instance.setValue(4, a.predict()); //prediction value
		instance.setValue(5, evaluationValue); //global evaluation
		
		return instance;
	}
	
	public int calculateFit(Action a) {
		if (WekaPlanner.isOnlyLearning()) {
			return randomData.nextInt(0, 100);
		}
		else {
			//WEKA evaluation
			Instance instance = createInstance(a, Instance.missingValue());
			try {
				if (evaluation == null) {
					System.out.println ("EVAL == NULL");
				}
				return (int) (evaluation.evaluateModelOnce(classifier, instance) *100);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return 0;
		
	
		
		//return a.estimate()+ a.predict();
	}

	public static boolean isOnlyLearning() {
		return onlyLearning;
	}

	public static void setOnlyLearning(boolean onlyLearning) {
		WekaPlanner.onlyLearning = onlyLearning;
	}
}
