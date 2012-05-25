package at.ac.tuwien.lsdc.actions;

import java.io.File;
import java.io.IOException;

import at.ac.tuwien.lsdc.resources.App;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;

public abstract class Action {
	private Instances knowledgeBase;
	public Instances getKnowledgeBase() {
		return knowledgeBase;
	}

	public void setKnowledgeBase(Instances knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public abstract void init(App problemApp);
	
	public abstract int predict();
	
	public abstract int estimate();
	
	public abstract boolean preconditions();
	
	public abstract void execute();
	
	
	//read the knowledgebase to an arff - file
	public void loadKnowledge(String filepath) throws Exception {
		 DataSource source = new DataSource(filepath);
		 
		 knowledgeBase = source.getDataSet();

		 // setting class attribute if the data format does not provide this information
		 // For example, the XRFF format saves the class attribute information as well
		 if (knowledgeBase.classIndex() == -1)
			 knowledgeBase.setClassIndex(knowledgeBase.numAttributes() - 1);
		 
		 
		 
	}
	
	//saves the knowledgebase to an arff - file
	//TODO: Problem bei Zurückschreiben in die gleiche Datei 
	public void saveKnowledge(String filepath) throws IOException {
		 ArffSaver saver = new ArffSaver();
		 saver.setInstances(knowledgeBase);
		 File file = new File(filepath);
		 if(file.exists()) {
			 System.out.println("delete");
			 System.out.println(file.delete());
		 }
		 
		 saver.setFile(file);
		 saver.writeBatch();
	}
	
	
}
