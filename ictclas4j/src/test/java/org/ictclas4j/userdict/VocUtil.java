package org.ictclas4j.userdict;

import java.util.Arrays;

import org.ictclas4j.bean.DictLib;
import org.ictclas4j.bean.SegAtom;
import org.ictclas4j.util.Utility;

public class VocUtil {
	public static void main(String[] args) {
		String word = "mapreduc";
		DictLib dictLib = new DictLib(false);
		System.out.println(word + " ==> " + computePossibility(dictLib, word));
	}
	
	public static double computePossibility(DictLib dictLib, String word) {
		final double smoothParam = 0.1;
		
		double[] posses = new double[word.length() - 1];
		for (int i = 0; i < word.length() - 1; i++) {
			String ch = word.substring(i, i+1);
			int gbkId = dictLib.getGBKID(ch);
			SegAtom sa = dictLib.getCoreDict().getSegAtom(ch, gbkId);
			int curFreq = sa == null? 0 : sa.getTotalFreq();
			String twoWords = ch + Utility.WORD_SEGMENTER + word.substring(i+1, i+2);
			
			// 计算相临两个词之间的平滑值
			// -log{a*P(Ci-1)+(1-a)P(Ci|Ci-1)} Note 0<a<1
			int twoFreq = dictLib.getBigramDict().getFreq(twoWords, 3, gbkId);
			double temp = (double) 1 / Utility.MAX_FREQUENCE;
			double value = smoothParam * (1 + curFreq) / (Utility.MAX_FREQUENCE + 80000);
			value += (1 - smoothParam) * ((1 - temp) * twoFreq / (1 + curFreq) + temp);
			value = -Math.log(value);
			
			posses[i] = value;
		}
		Arrays.sort(posses);
		double poss = 0;
		for (int i = 0; i < 3 && i < posses.length; i++) poss += posses[i];
		return poss / Math.min(3, posses.length);
	}
}