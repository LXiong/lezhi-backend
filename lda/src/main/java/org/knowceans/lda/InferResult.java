package org.knowceans.lda;

public class InferResult {
	private double likelihood;
	private double[] varGamma;
	private double[][] phi;
	
	public InferResult(double likelihood, double[] varGamma, double[][] phi) {
		this.likelihood = likelihood;
		this.varGamma = varGamma;
		this.phi = phi;
	}

	public double getLikelihood() {
		return likelihood;
	}

	public double[] getVarGamma() {
		return varGamma;
	}

	public double[][] getPhi() {
		return phi;
	}
}