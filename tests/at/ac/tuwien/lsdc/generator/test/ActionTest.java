package at.ac.tuwien.lsdc.generator.test;

import static org.junit.Assert.*;

import org.junit.Test;

import at.ac.tuwien.lsdc.actions.CreateVmInsertApp;

public class ActionTest { 

	@Test
	public void testArffReader() {
		CreateVmInsertApp action = new CreateVmInsertApp();
		try {
			String filepath = "knowledge/cpu.arff";
			action.loadKnowledge(filepath);
			System.out.println("loading complete!");
			action.saveKnowledge(filepath, action.getKnowledgeBase());
			System.out.println("saving complete!");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
