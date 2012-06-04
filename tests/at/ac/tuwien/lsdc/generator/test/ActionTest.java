package at.ac.tuwien.lsdc.generator.test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

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
	
	@Test
	public void testIntegerToFloat() {
		int a = 10;
		int b = 20;
		
		assertSame(50, (int) (a / (float) b * 100));
	}

}
