package com.buzzinate.common.diffusion;

public class Diffusions {
	public static Algo[] algos = new Algo[] {
		new Algo(new MyDiffusion(0.3, 0.7), "heat"),
		new Algo(new MyDiffusion(0.7, 0.3), "energy"),
	};
	
	private Diffusions() {}
	
	public static class Algo {
		public Diffusion diffusion;
		public String name;
		
		public Algo(Diffusion diffusion, String name) {
			this.diffusion = diffusion;
			this.name = name;
		}
	}
}