package at.ac.tuwien.lsdc.generator.test;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Test;

import at.ac.tuwien.lsdc.resources.Resource;
import at.ac.tuwien.lsdc.resources.VirtualMachine;

public class ResourceTest {

	@Test
	//sum up:
	//l1: 1, 2, 3, 4, 5
	//l2:       1, 4, 5
	//l3:
	//out:1, 2, 4, 8, 10
	public void testAggregate() {
		System.out.println ("testAggregate:");
		LinkedList<Integer> l1 = new LinkedList<Integer>();
		l1.addLast(1);
		l1.addLast(2);
		l1.addLast(3);
		l1.addLast(4);
		l1.addLast(5);
		
		LinkedList<Integer> l2 = new LinkedList<Integer>();
		l2.addLast(1);
		l2.addLast(4);
		l2.addLast(5);
		
		LinkedList<Integer> l3 = new LinkedList<Integer>();
		LinkedList<LinkedList<Integer>> in =new  LinkedList<LinkedList<Integer>>();
		in.add(l1);
		in.add(l2);
		in.add(l3);
		
		LinkedList<Integer> out = Resource.aggregateValues(in);
		System.out.println(out.size());
		String s="";
		for (Integer i1 : out) {
			s+=i1 + ";";
		}
		System.out.println(s);
		assertTrue(out.size()==5);
		assertTrue(out.get(0)==1);
		assertTrue(out.get(1)==2);
		assertTrue(out.get(2)==4);
		assertTrue(out.get(3)==8);
		assertTrue(out.get(4)==10);
	}
	
	@Test
	//get last 3 entries
	//l1: 1, 2, 3, 4, 5
	//out:3,4,5
	public void testGetLastEntriesUtil() {
		System.out.println ("testGetLastEntriesUtil:");
		LinkedList<Integer> l1 = new LinkedList<Integer>();
		l1.addLast(1);
		l1.addLast(2);
		l1.addLast(3);
		l1.addLast(4);
		l1.addLast(5);
		
		LinkedList<Integer> out = Resource.getLastEntriesUtil(l1, 3);
		
		String s="";
		for (Integer i1: l1) {
			s+=i1 + ";";
		}
		System.out.println(s);
		
		s="";
		for (Integer i1: out) {
			s+=i1 + ";";
		}
		System.out.println (s);
		assertTrue(out.size()==3);
		assertTrue(out.get(0)==3);
		assertTrue(out.get(1)==4);
		assertTrue(out.get(2)==5);
		
		out = Resource.getLastEntriesUtil(l1, 0);
		assertTrue(out.size()==0);
		
		out = Resource.getLastEntriesUtil(l1, 10);
		assertTrue(out.size()==5);
		
	}

}
