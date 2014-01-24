package com.buzzinate.common.model;

import com.google.code.morphia.annotations.Embedded;

@Embedded
public class Minhash {
	private int hash;
	private int size;
	
	public Minhash() {}
	
	public Minhash(int hash, int size) {
		this.hash = hash;
		this.size = size;
	}

	public int getHash() {
		return hash;
	}

	public void setHash(int hash) {
		this.hash = hash;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return hash + "(" + size + ")";
	}
}