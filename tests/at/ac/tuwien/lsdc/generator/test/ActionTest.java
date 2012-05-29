package at.ac.tuwien.lsdc.generator.test;

import static org.junit.Assert.*;

import org.junit.Test;

import at.ac.tuwien.lsdc.actions.CreateVmInsertApp;

public class ActionTest {

	@Test
	public void testArffReader() {
		CreateVmInsertApp action = new CreateVmInsertApp();
		try {
			action.loadKnowledge("knowledge/cpu.arff");
//			System.out.println(action.getKnowledgeBase().firstInstance());
			Thread.sleep(1000);
//			System.out.println(action.getKnowledgeBase());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
