package org.ictclas4j.bean;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.ictclas4j.util.IntHashMap;
import org.ictclas4j.util.IntIntHashMap;

import com.gftech.util.GFNet;

/**
 * 词性上下文，描述词性在上下文中的关系，如：词性单独出现的频率、相邻出现的频率等
 * 
 * @author sinboy
 * @since 2008.6.4
 * 
 */
public class PosContext {

	private int totalPosFreq;// 所有词性出现次数的总和

	private IntHashMap<Integer> freqMap;// 词频表,词性为Key，词频为Value

	private IntIntHashMap<Integer> adjoiningFreqMap;// 邻接词频表,用第I个词性的值加第J个词性的值做为Key，例如：“123843,892342”

	public PosContext() {
		this(null);
	}

	public PosContext(String fileName) {
		freqMap = new IntHashMap<Integer>(14177, 0.5f);
		adjoiningFreqMap = new IntIntHashMap<Integer>(14177, 0.5f);
		load(fileName);
	}

	public boolean load(String fileName) {
		if (fileName != null) {
			try {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				DataInputStream in = new DataInputStream(new BufferedInputStream(cl.getResourceAsStream(fileName)));

				// 读取总词频
				totalPosFreq = GFNet.readInt32(in);
				// 读取长度
				int posCount = GFNet.readInt32(in);
//				logger.debug("tableLen:" + posCount);

				// 读取词性和词频
				int[] posTable = new int[posCount];
				for (int i = 0; i < posCount; i++) {
					posTable[i] = GFNet.readInt32(in);
					int pos = posTable[i];
					int freq = GFNet.readInt32(in);
					freqMap.put(pos, freq);
//					logger.debug("pos[" + i + "]:" + pos + "/" + POSTag.int2str(pos) + ",freq:" + freq);
				}
				
				// 读取邻接词频
				for (int i = 0; i < posCount; i++) {
					for (int j = 0; j < posCount; j++) {
						int adjoiningFreq = GFNet.readInt32(in);
						adjoiningFreqMap.put(posTable[i], posTable[j], adjoiningFreq);
					}
				}

				in.close();
				return true;
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	public int getTotalPosFreq() {
		return totalPosFreq;
	}

	/**
	 * 词性频率
	 * 
	 * @param pos
	 * @return
	 */
	public int getFreq(int pos) {
		if (freqMap != null) {
			Integer value = freqMap.get(pos);
			if (value != null)
				return value;
		}

		return 0;
	}

	// 邻接词性频率
	public int getAdjoiningFreq(int prevPos, int curPos) {
		Integer freq = adjoiningFreqMap.get(prevPos, curPos);
		if (freq != null) {
			return freq;
		}
		return 0;
	}

	// 计算两个词性邻接的可能性
	public double computePossibility(int prevPos, int curPos) {
		double result = 0;
		// return a lower value, not 0 to prevent data sparse
		int prevFreq = getFreq(prevPos);
		if (prevFreq == 0 || getFreq(curPos) == 0)
			return 0.000001;

		int adjoiningFreq = getAdjoiningFreq(prevPos, curPos);

		// 0.9 and 0.1 is a value based experience
		result = 0.9 * (double) adjoiningFreq;
		result /= (double) prevFreq;
		result += 0.1 * ((double) prevFreq / totalPosFreq);

		return result;
	}
}