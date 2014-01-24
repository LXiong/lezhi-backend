package com.buzzinate.nlp.matrix.scoring;

public class ScoreResult {
	private double[] rowScore;
	private double[] colScore;
	
	public ScoreResult(double[] rowScore, double[] colScore) {
		this.rowScore = rowScore;
		this.colScore = colScore;
	}

	public double[] getRowScore() {
		return rowScore;
	}

	public double[] getColScore() {
		return colScore;
	}
}