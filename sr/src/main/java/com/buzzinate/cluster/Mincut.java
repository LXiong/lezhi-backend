package com.buzzinate.cluster;

import java.util.Arrays;

public class Mincut {

	public static void main(String[] args) {
		double[][] g = new double[][] {
			new double[]{0, 3, 2, 1},
			new double[]{3, 0, 1, 1},
			new double[]{2, 1, 0, 1},
			new double[]{1, 1, 1, 0}
		};
		System.out.println(Arrays.toString(mincut(g, 0, 3)));
	}

	public static int[] mincut(double[][] g, int p, int max) {
		int[] res = new int[max];
		int n = g.length;
				
		boolean[] mark = new boolean[n];
		Arrays.fill(mark, false);
		mark[p] = true;
		
		double[] dist = new double[n];
		for (int i = 0; i < dist.length; i++) {
			dist[i] = 0;
			for (int j = 0; j < dist.length; j++) dist[i] += g[i][j];
		}
		
		int k = 0;
		res[k++] = p;
		while (k < max) {
			double min = Double.MAX_VALUE;
			int mini = -1;
			for (int i = 0; i < n; i++) {
				if (mark[i]) continue;
				double nc = dist[p] + dist[i] - g[p][i] * 2;
				if (nc < min) {
					min = nc;
					mini = i;
				}
			}
			if (mini != -1) {
				res[k++] = mini;
				// update dist[p]
				dist[p] = min;
				
				// remove mini
				mark[mini] = true;
				
				// update g
				for (int i = 0; i < n; i++) {
					if (mark[i]) continue;
					g[i][p] += g[i][mini];
					g[p][i] = g[i][p];
				}
				
			}
		}
		return res;
	}
}
