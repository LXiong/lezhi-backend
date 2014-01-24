package org.ictclas4j.bean;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.ictclas4j.util.TrieMap;
import org.ictclas4j.util.Utility;

import com.gftech.util.GFCommon;
import com.gftech.util.GFNet;
import com.gftech.util.GFString;

public class Dictionary {
	/**
	 * 词典表,共6768个,GB2312编码(before) 22034个，gbk编码+字母数字（now)
	 */
	private WordTable[] wts;
	
	private TrieMap<SegAtom> none = new TrieMap<SegAtom>();

	private int wordCount;// 词的个数

	private long totalFreq;// 总词频

	private int dict_count;

	public Dictionary() {
		this(null,false);
	}
	public Dictionary(String fileName) {
		this(fileName,false);
	}
	
	public Dictionary( boolean isExtend) {
		this(null,isExtend);
	}
	
	public Dictionary(String fileName,boolean isExtend) {
		init(isExtend);
		load(fileName);
	}

	public void init(boolean isExtend) {
		wordCount = 0;
		totalFreq = 0;
		dict_count = isExtend ? Utility.GBK_NUM_EXT : Utility.GB_NUM;
		wts = new WordTable[dict_count];

	}

	/**
	 * 从词典表中加载词条.共6768个大的数据块(包括5个非汉字字符),每个大数据块包括若干个小数据块,
	 * 每个小数据块为一个词条,该数据块中每个词条都是共一个字开头的.
	 * 
	 * @param fileName
	 *            核心词典文件名
	 * @return
	 */
	public boolean load(String fileName) {
		int i = 0, j = 0;
		try {
			WordTable wt = new WordTable();
			SegAtom sa = new SegAtom();
			TrieMap<SegAtom> wordMap = null;
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			InputStream is = cl.getResourceAsStream(fileName);
			DataInputStream in = new DataInputStream(new BufferedInputStream(is));
			for (i = 0; i < dict_count; i++) {
				try {
					WordTable wtClone = wt.clone();
//					logger.debug("块" + i);
					// 词典库在写二进制数据时采用低位优先(小头在前)方式,需要转换一下
					int count = GFNet.readInt32(in);
//					logger.debug(" count:" + count);
					wtClone.setWordCount(count);
					int wordMaxLen = GFNet.readUInt8(in);
					wtClone.setWordMaxLen(wordMaxLen);
					wordMap = new TrieMap<SegAtom>();
					for (j = 0; j < count; j++, wordCount++) {
						SegAtom saClone = sa.clone();
						saClone.read(in);
//						logger.debug(saClone);
						wordMap.put(saClone.getWord(), saClone);
						totalFreq += saClone.getTotalFreq();
					}
					wtClone.setWordMap(wordMap);
					wts[i] = wtClone;
				} catch (CloneNotSupportedException e) {
					throw new RuntimeException("Load dict:", e);
				}
			}

			in.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("load dict " + fileName + ":", e);
		} catch (IOException e) {
			throw new RuntimeException("load dict " + fileName + ":", e);
		}

		return true;
	}
	
	public WordTable getWordTable(int index) {
		if (index > 0 && index < wts.length) {
			return wts[index];
		}
		return null;
	}

	public SegAtom getSegAtom(String word, int index) {
		SegAtom result = null;

		if (word != null && word.length() > 0) {
			if (index > 0 && index < wts.length) {
				WordTable wt = wts[index];
				result = wt.getSegAtom(word);

			}
		}
		return result;
	}
	
	public TrieMap<SegAtom> getwt(int index) {
		if (index > 0 && index < wts.length) {
			return wts[index].getWordMap();
		} else return none;
	}
	
	public boolean addSegAtom(SegAtom sa,int index){
		
		if(sa!=null && index>=0 && index<dict_count){
			if(wts!=null){
				WordTable wt=wts[index];
				if(wt!=null){
					wt.addSegAtom(sa);
				}
			}
		}
		return false;
	}

	// 获取同一个字开头、长度最大的关键词长度
	public int getWordMaxLen(String word, int index) {
		int result = 0;
		if (word != null && word.length() > 0) {
			if (index > 0 && index < wts.length) {
				WordTable wt = wts[index];
				return wt.getWordMaxLen();
			}
		}
		return result;
	}

	public boolean strEqual(String b1, String b2) {
		if (b1 == null && b2 == null)
			return true;
		else if (b1 != null && b2 != null) {
			return b1.equals(b2);
		}
		return false;
	}

	public int getWordType(String word) {
		if (word != null) {
			int type = Utility.charType(word);
			int len = word.length();

			if (len > 0 && type == Utility.CT_CHINESE && GFString.isAllChinese(word))
				return Utility.WT_CHINESE;
			else if (len > 0 && type == Utility.CT_DELIMITER)
				return Utility.WT_DELIMITER;

		}
		return Utility.WT_OTHER;
	}

	/**
	 * 判断关键词是否存在该词性
	 * 
	 * @param word
	 * @param pos
	 * @return
	 */
	public boolean isExist(String word, int pos, int index) {
		if (word != null) {
			SegAtom atom = getSegAtom(word, index);
			if (atom != null) {
				return atom.hasPos(pos);
			}
		}

		return false;
	}

	public int getFreq(String word, int pos, int index) {
		if (word != null) {
			SegAtom atom = getSegAtom(word, index);
			if (atom != null) {
				return atom.getFreqByPos(pos);
			}
		}
		return 0;
	}

	public long totalFreq() {
		return totalFreq;
	}

	public int wordCount() {
		return wordCount;
	}

	public WordTable[] getWts() {
		return wts;
	}

	public void setWts(WordTable[] wts) {
		this.wts = wts;
	}

}
