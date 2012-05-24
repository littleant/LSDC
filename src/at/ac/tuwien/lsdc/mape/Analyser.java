package at.ac.tuwien.lsdc.mape;

import java.util.List;

import at.ac.tuwien.lsdc.generator.Request;
import at.ac.tuwien.lsdc.generator.RequestGenerator;

public class Analyser {
	public Problem getTopProblem() {
		// Check SLAs against real data (red, orange, green)
		// TODO
		
		// Check if there are one or more requests in the queue
		RequestGenerator requestGenerator = RequestGenerator.getInstance();
		List<Request> requests = requestGenerator.getRequests();
		// TODO
		
		// Decide what the top problem is
		// TODO
		
		return null;
	}
}
