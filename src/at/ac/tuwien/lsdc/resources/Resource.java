package at.ac.tuwien.lsdc.resources;

import java.util.Iterator;
import java.util.LinkedList;

public abstract class Resource {
	protected int suspendedTicks=0;
	protected int runningTicks=0;
	
	//returns the last n elements of an integer list
	protected LinkedList<Integer> getLastEntriesUtil(LinkedList<Integer> input,int maxNoOfEntries) {
		LinkedList<Integer> out = new LinkedList<Integer>();
		int i=0;
		Iterator<Integer> it = input.descendingIterator();
		while (it.hasNext() && i<maxNoOfEntries){
			out.addFirst(it.next());
		}
		return out;
	}
	
	
	protected LinkedList<Integer> aggregateValues(LinkedList<LinkedList<Integer>> input ) {
		boolean allFinished = false;
		int i = 1;
		int curVal = 0;
		
		while (allFinished==false) {
			allFinished = true;
			curVal = 0;
			
			for (LinkedList<Integer> li : input) {
				if (li.size()>=i) {
					allFinished=false;
					curVal += li.get(li.size()-i);
				}
			}
			i++;
		}
		return null;
	}
}
