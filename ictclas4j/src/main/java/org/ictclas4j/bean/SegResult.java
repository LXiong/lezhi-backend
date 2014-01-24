package org.ictclas4j.bean;

import java.util.List;

/**
 * 分词结果，包括分词后的词性、词频
 * 
 * @author sinboy
 * 
 */
public class SegResult {

	private String rawContent;// 原始内容
	private List<SegAtom> atoms; // 经过分词后的原子单元

	public SegResult(String rawContent, List<SegAtom> atoms) {
		this.rawContent = rawContent;
		this.atoms = atoms;
	}
	
	public String getRawContent() {
		return rawContent;
	}

	public List<SegAtom> getAtoms() {
		return atoms;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		if (atoms != null) {
			for (int i = 0; i < atoms.size(); i++) {
				SegAtom atom = atoms.get(i);
				if(atom!=null &&("始##始".equals(atom.getWord())||"末##末".equals(atom.getWord()))) continue;
				sb.append(atom.toString());
				if (i < atoms.size() - 1) sb.append(" ");
			}
		}

		return sb.toString();
	}
}