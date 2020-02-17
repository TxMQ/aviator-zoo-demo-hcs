package com.txmq.exozoodemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.txmq.aviator.core.AviatorStateBase;


/**
 * This holds the current state of the swirld. For this simple "hello swirld" code, each transaction is just
 * a string, and the state is just a list of the strings in all the transactions handled so far, in the
 * order that they were handled.
 */
public class SocketDemoState extends AviatorStateBase {
	
	
	/**
	 * The zoo consists of a number of lions, tigers, and bears pushed into the zoo by users
	 */
	private List<String> lions = Collections
			.synchronizedList(new ArrayList<String>());

	/** @return all the strings received so far from the network */
	public synchronized List<String> getLions() {
		return lions;
	}

	public synchronized void addLion(String name) {
		this.lions.add(name);
	}
	
	private List<String> tigers = Collections
			.synchronizedList(new ArrayList<String>());

	/** @return all the strings received so far from the network */
	public synchronized List<String> getTigers() {
		return tigers;
	}

	public synchronized void addTiger(String name) {
		this.tigers.add(name);
	}
	
	private List<String> bears = Collections
			.synchronizedList(new ArrayList<String>());

	/** @return all the strings received so far from the network */
	public synchronized List<String> getBears() {
		return bears;
	}
	
	public synchronized void addBear(String name) {
		this.bears.add(name);
	}

	// ///////////////////////////////////////////////////////////////////

	

	@Override
	public synchronized void copyFrom(AviatorStateBase old) {
		super.copyFrom(old);
		lions = Collections.synchronizedList(new ArrayList<String>(((SocketDemoState) old).lions));
		tigers = Collections.synchronizedList(new ArrayList<String>(((SocketDemoState) old).tigers));
		bears= Collections.synchronizedList(new ArrayList<String>(((SocketDemoState) old).bears));
	}

}	