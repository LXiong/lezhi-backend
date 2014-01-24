package com.buzzinate.jianghu.sr;

public class ItemScore {
	public long id;
	public double score;
	public long createAt;
	
	public ItemScore() {
	}
	
	public ItemScore(long id, double score, long createAt) {
		this.id = id;
		this.score = score;
		this.createAt = createAt;
	}
	
	@Override
	public String toString() {
		return id + "(" + score + ", " + createAt + ")";
	}
}