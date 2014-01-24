package org.ictclas4j.bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.ictclas4j.util.CharHashMap;
import org.ictclas4j.util.Utility;
import org.ictclas4j.util.Utility.TAG_TYPE;


/**
 * Dictionary Library
 * 
 * @author sinboy
 * @since 2007.12.6
 * 
 */
public class DictLib {
	private static final String NOUN = "n";

	private static final int AVERAGE_FREQ = 14;

	private Dictionary coreDict;

	private Dictionary bigramDict;

	private Dictionary personUnknownDict;

	private PosContext personContext;

	private Dictionary transPersonUnknownDict;

	private PosContext transPersonContext;

	private Dictionary placeUnknownDict;

	private PosContext placeContext;

	private Dictionary lexUnknownDict;

	private PosContext lexContext;

	// GBK汉字+字母数字对的GBK_ID值
	private CharHashMap<Integer> idMap;
	
	public DictLib() {
		this(true);
	}

	public DictLib(boolean useMyDict) {
		boolean isGBKExtend = false;
		idMap = new CharHashMap<Integer>(Utility.GBK_NUM_EXT);
		for (int i = 0; i < Utility.GBK_NUM_EXT; i++) {
			idMap.put(Utility.getGBKWord(i).charAt(0), i);
		}

		// logger.info("Load coreDict  ...");
		coreDict = new Dictionary("data/coreDict.dct", isGBKExtend);

		// logger.info("Load bigramDict ...");
		bigramDict = new Dictionary("data/bigramDict.dct", isGBKExtend);

		// logger.info("Load tagger dict ...");
		personUnknownDict = new Dictionary("data/nr.dct", isGBKExtend);
		personContext = new PosContext("data/nr.ctx");
		transPersonUnknownDict = new Dictionary("data/tr.dct", isGBKExtend);
		transPersonContext = new PosContext("data/tr.ctx");
		placeUnknownDict = new Dictionary("data/ns.dct", isGBKExtend);
		placeContext = new PosContext("data/ns.ctx");
		lexUnknownDict = coreDict;
		lexContext = new PosContext("data/lexical.ctx");

		if (useMyDict) loadMyDict("data/userDict.txt");
		// personTagger = new PosTagger(Utility.TAG_TYPE.TT_PERSON, "data" +
		// GFFinal.FILE_SEP + "nr", coreDict);
		// transPersonTagger = new PosTagger(Utility.TAG_TYPE.TT_TRANS_PERSON,
		// "data" + GFFinal.FILE_SEP + "tr", coreDict);
		// placeTagger = new PosTagger(Utility.TAG_TYPE.TT_TRANS_PERSON, "data"
		// + GFFinal.FILE_SEP + "ns", coreDict);
		// lexTagger = new PosTagger(Utility.TAG_TYPE.TT_NORMAL, "data" +
		// GFFinal.FILE_SEP + "lexical", coreDict);

		// pronunDict = new PronunDict("data"+GFFinal.FILE_SEP+"pronun.txt");
		// logger.info("Load dict is over");
	}

	public Dictionary getBigramDict() {
		return bigramDict;
	}

	public Dictionary getCoreDict() {
		return coreDict;
	}

	public Dictionary getPersonUnknownDict() {
		return personUnknownDict;
	}

	public PosContext getPersonContext() {
		return personContext;
	}

	public Dictionary getTransPersonUnknownDict() {
		return transPersonUnknownDict;
	}

	public PosContext getTransPersonContext() {
		return transPersonContext;
	}

	public Dictionary getPlaceUnknownDict() {
		return placeUnknownDict;
	}

	public PosContext getPlaceContext() {
		return placeContext;
	}

	public Dictionary getLexUnknownDict() {
		return lexUnknownDict;
	}

	public PosContext getLexContext() {
		return lexContext;
	}

	public Dictionary getUnknownDict(TAG_TYPE type) {
		switch (type) {
		case TT_PERSON:
			return this.personUnknownDict;
		case TT_TRANS_PERSON:
			return this.transPersonUnknownDict;
		case TT_PLACE:
			return this.placeUnknownDict;
		default:
			return this.lexUnknownDict;
		}
	}

	public PosContext getContext(TAG_TYPE type) {
		switch (type) {
		case TT_PERSON:
			return this.personContext;
		case TT_TRANS_PERSON:
			return this.transPersonContext;
		case TT_PLACE:
			return this.placeContext;
		default:
			return this.lexContext;
		}
	}

	// TODO:
	public boolean addWordItem(SegAtom wi, boolean isOvercast, boolean isNotSave) {
		// if (wi != null && coreDict != null) {
		// int handle = wi.getHandle();
		// return coreDict.addItem(wi.getWord(), handle, wi.getFreq(), false,
		// isOvercast, isNotSave);
		// } else
		return false;
	}

	// TODO:
	public boolean addBigramWordItem(SegAtom wi, boolean isNotSave) {
		// if (wi != null && bigramDict != null) {
		// int handle = wi.getHandle();
		// return bigramDict.addItem(wi.getWord(), handle, wi.getFreq(), false,
		// false, isNotSave);
		// } else
		return false;
	}

	// TODO:
	public boolean delWordItem(String word, int pos) {
		// if (word != null && coreDict != null) {
		// return coreDict.delItem(word, pos);
		// } else
		return false;
	}

	public int getGBKID(String word) {
		if (word != null && word.length() > 0) {
			char ch = word.charAt(0);
			Integer obj = idMap.get(ch);
			return obj != null ? obj : -1;
		}
		return -1;
	}

	// 加载用户自定义词组
	private void loadMyDict(String userDictName) {
		if (userDictName != null) {
			try {
				InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(userDictName);
				BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String word = null;
				SegAtom sa = new SegAtom();
				while((word = br.readLine()) != null){
					SegAtom saClone = sa.clone();
					saClone.setWord(word.trim());
					Pos pos = new Pos();
					pos.setTag(POSTag.str2int(NOUN));
					pos.setFreq(AVERAGE_FREQ);
					pos.setVisible(true);
					saClone.addPos(pos);
					int index = getGBKID(word.trim());
					coreDict.addSegAtom(saClone, index);
				}
				br.close();
				is.close();
				
			} catch (IOException e) {
				throw new RuntimeException("load myDict is failed", e);
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}