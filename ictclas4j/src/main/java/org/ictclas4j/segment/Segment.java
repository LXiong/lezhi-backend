package org.ictclas4j.segment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ictclas4j.bean.Atom;
import org.ictclas4j.bean.DictLib;
import org.ictclas4j.bean.POSTag;
import org.ictclas4j.bean.Pos;
import org.ictclas4j.bean.SegAtom;
import org.ictclas4j.bean.SegNode;
import org.ictclas4j.bean.SegResult;
import org.ictclas4j.bean.Sentence;
import org.ictclas4j.util.TrieMap.Node;
import org.ictclas4j.util.Utility;


public class Segment {
	private DictLib dictLib;

	public Segment(DictLib dictLib) {
		this.dictLib = dictLib;
	}
	
	public SegResult split(String src) {
		return split(src, true);
	}

	// isRecogniseUnknown 是否识别未登录词
	public SegResult split(String src, boolean isRecogniseUnknown) {
		List<SegAtom> finalAtoms = new ArrayList<SegAtom>();// 分词结果

		if (src != null) {
			List<SegAtom> midResult = null;
			SentenceSeg ss = new SentenceSeg(src);
			ArrayList<Sentence> sens = ss.getSens();

			for (Sentence sen : sens) {
				if (sen.isSeg()) {
					// 原子分词
					AtomSeg as = new AtomSeg(sen.getContent());
					ArrayList<Atom> atoms = as.getAtoms();
					
					// 生成分词图表,先进行初步分词，然后进行优化，最后进行词性标记
					SegGraph segGraph = GraphGenerate.generate(atoms, dictLib);
					// 生成二叉分词图表
					SegGraph biSegGraph = GraphGenerate.biGenerate(segGraph, dictLib);
					
					// 求N最短路径
					NShortPath nsp = new NShortPath(biSegGraph);
					ArrayList<ArrayList<Integer>> bipath = nsp.getPaths();
					
					for (ArrayList<Integer> onePath : bipath) {
						// 得到初次分词路径
						ArrayList<SegNode> segPath = getSegPath(segGraph, onePath);
						ArrayList<SegNode> firstPath = AdjustSeg.firstAdjust(segPath);
						
						if (isRecogniseUnknown)
							midResult = optinium(firstPath);
						else {
							PosTagger lexTagger = new PosTagger(Utility.TAG_TYPE.TT_NORMAL, dictLib);
							lexTagger.recognise(firstPath);
							ArrayList<SegNode> adjResult = AdjustSeg.finalAdjust(firstPath, dictLib);
							midResult = outputResult(adjResult);
						}
						break;
					}
				} else {
					SegAtom atom = new SegAtom(sen.getContent());
					SegAtom[] atoms = new SegAtom[1];
					atoms[0] = atom;
					midResult = Arrays.asList(atom);
				}
				finalAtoms.addAll(midResult);
			}
		}

		return new SegResult(src, finalAtoms);
	}

	public List<Pos> getPoses(String word) {
		int gbkId = dictLib.getGBKID(word);
		if (gbkId == -1) return new ArrayList<Pos>(); // TODO: handle English
		Node<SegAtom> node = dictLib.getCoreDict().getwt(gbkId).search(word);
		if (node == null) return new ArrayList<Pos>();
		else if (node.getValue() == null) {
//			System.out.println("Doesn't have such word: " + word);
			return new ArrayList<Pos>();
		}
		else return node.getValue().getPosList();
	}
	
	// 对初次分词结果进行优化
	private List<SegAtom> optinium(ArrayList<SegNode> firstPath) {
		List<SegAtom> result = null;
		if (firstPath != null) {
			// 处理未登陆词，进对初次分词结果进行优化
			SegGraph optSegGraph = new SegGraph(firstPath);
			ArrayList<SegNode> sns = firstPath;
			PosTagger personTagger = new PosTagger(Utility.TAG_TYPE.TT_PERSON, dictLib);
			personTagger.recognise(optSegGraph, sns);
			PosTagger transPersonTagger = new PosTagger(Utility.TAG_TYPE.TT_TRANS_PERSON, dictLib);
			transPersonTagger.recognise(optSegGraph, sns);
			// PosTagger placeTagger=new
			// PosTagger(Utility.TAG_TYPE.TT_PLACE,dictLib);
			// placeTagger.recognise(optSegGraph, sns);
			
			// 根据优化后的结果，重新进行生成二叉分词图表
			SegGraph optBiSegGraph = GraphGenerate.biGenerate(optSegGraph, dictLib);
			
			// 重新求取N－最短路径
			NShortPath optNsp = new NShortPath(optBiSegGraph);
			ArrayList<ArrayList<Integer>> optBipath = optNsp.getPaths();
			
			// 生成优化后的分词结果，并对结果进行词性标记和最后的优化调整处理
			ArrayList<SegNode> adjResult = null;
			PosTagger lexTagger = new PosTagger(Utility.TAG_TYPE.TT_NORMAL, dictLib);
			for (ArrayList<Integer> optOnePath : optBipath) {
				ArrayList<SegNode> optSegPath = getSegPath(optSegGraph, optOnePath);
				lexTagger.recognise(optSegPath);
				adjResult = AdjustSeg.finalAdjust(optSegPath, dictLib);
				result = outputResult(adjResult);
				break;
			}
		}
		return result;
	}

	// 根据二叉分词路径生成分词路径
	private ArrayList<SegNode> getSegPath(SegGraph sg, ArrayList<Integer> bipath) {

		ArrayList<SegNode> path = null;

		if (sg != null && bipath != null) {
			ArrayList<SegNode> sns = sg.getSnList();
			path = new ArrayList<SegNode>();

			for (int index : bipath)
				path.add(sns.get(index));

		}
		return path;
	}

	// 根据分词路径生成分词结果
	private List<SegAtom> outputResult(ArrayList<SegNode> wrList) {
		List<SegAtom> result = null;
		if (wrList != null && wrList.size() > 0) {
			result = new ArrayList<SegAtom>();
			for (int i = 0; i < wrList.size(); i++) {

				SegNode sn = wrList.get(i);
				if (sn.getPos() != POSTag.SEN_BEGIN && sn.getPos() != POSTag.SEN_END) { 
					SegAtom sa =sn.toSegAtom();
					result.add(sa);
				} 
			}
		}
		return result;
	}
}