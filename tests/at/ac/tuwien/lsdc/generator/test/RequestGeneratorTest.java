package at.ac.tuwien.lsdc.generator.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import at.ac.tuwien.lsdc.generator.RequestGenerator;

public class RequestGeneratorTest {
	@Test
	public void testSingleton() {
		assertTrue((RequestGenerator.getInstance() == RequestGenerator.getInstance()));
	}
}
