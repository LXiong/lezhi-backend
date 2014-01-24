package org.ictclas4j.userdict;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ictclas4j.bean.DictLib;
import org.ictclas4j.bean.POSTag;
import org.ictclas4j.bean.Pos;
import org.ictclas4j.bean.SegAtom;
import org.ictclas4j.bean.SegResult;
import org.ictclas4j.segment.Segment;


public class SougouScelReader {

	protected ByteArrayOutputStream output = new ByteArrayOutputStream();

	protected String readString(DataInputStream input, int pos, int[] reads)
			throws IOException {
		int read = reads[0];
		input.skip(pos - read);
		read = pos;
		output.reset();
		while (true) {
			int c1 = input.read();
			int c2 = input.read();
			read += 2;
			if (c1 == 0 && c2 == 0) {
				break;
			} else {
				output.write(c1);
				output.write(c2);
			}
		}
		reads[0] = read;
		return new String(output.toByteArray(), encoding);
	}

	protected static String encoding = "UTF-16LE";

	public SougouScelMdel read(DataInputStream input) throws IOException {
		SougouScelMdel model = new SougouScelMdel();
		// DataInputStream input = new DataInputStream(in);
		int read;
		try {
			byte[] bytes = new byte[4];
			input.readFully(bytes);
			assert (bytes[0] == 0x40 && bytes[1] == 0x15 && bytes[2] == 0 && bytes[3] == 0);
			input.readFully(bytes);
			int flag1 = bytes[0];
			assert (bytes[1] == 0x43 && bytes[2] == 0x53 && bytes[3] == 0x01);
			int[] reads = new int[] { 8 };
			model.setName(readString(input, 0x130, reads));
			model.setType(readString(input, 0x338, reads));
			model.setDescription(readString(input, 0x540, reads));
			model.setSample(readString(input, 0xd40, reads));
			read = reads[0];
			input.skip(0x1540 - read);
			read = 0x1540;
			input.readFully(bytes);
			read += 4;
			assert (bytes[0] == (byte) 0x9D && bytes[1] == 0x01
					&& bytes[2] == 0 && bytes[3] == 0);
			bytes = new byte[128];
			Map<Integer, String> pyMap = new LinkedHashMap<Integer, String>();
			while (true) {
				int mark = readUnsignedShort(input);
				int size = input.readUnsignedByte();
				input.skip(1);
				read += 4;
				assert (size > 0 && (size % 2) == 0);
				input.readFully(bytes, 0, size);
				read += size;
				String py = new String(bytes, 0, size, encoding);
				// System.out.println(py);
				pyMap.put(mark, py);
				if ("zuo".equals(py)) {
					break;
				}
			}
			if (flag1 == 0x44) {
				input.skip(0x2628 - read);
			} else if (flag1 == 0x45) {
				input.skip(0x26C4 - read);
			} else {
				throw new RuntimeException("出现意外，联系作者");
			}
			StringBuffer buffer = new StringBuffer();
			Map<String, List<String>> wordMap = new LinkedHashMap<String, List<String>>();
			while (true) {
				int size = readUnsignedShort(input);
				if (size < 0) {
					break;
				}
				int count = readUnsignedShort(input);
				int len = count / 2;
				assert (len * 2 == count);
				buffer.setLength(0);
				for (int i = 0; i < len; i++) {
					int key = readUnsignedShort(input);
					buffer.append(pyMap.get(key)).append("'");
				}
				buffer.setLength(buffer.length() - 1);
				String py = buffer.toString();
				List<String> list = wordMap.get(py);
				if (list == null) {
					list = new ArrayList<String>();
					wordMap.put(py, list);
				}
				for (int i = 0; i < size; i++) {
					count = readUnsignedShort(input);
					if (count > bytes.length) {
						bytes = new byte[count];
					}
					input.readFully(bytes, 0, count);
					String word = new String(bytes, 0, count, encoding);
					// 接下来12个字节可能是词频或者类似信息
					input.skip(12);
					list.add(word);
				}
			}
			// System.out.println(wordMap.size());
			model.setWordMap(wordMap);
			return model;
		} finally {
			input.close();
		}
	}

	protected final int readUnsignedShort(InputStream in) throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0) {
			return Integer.MIN_VALUE;
		}
		return (ch2 << 8) + (ch1 << 0);
	}

	public void creatUserDict() throws FileNotFoundException, IOException {
		//ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String baseDir = "myDict";
		File file = new File(baseDir);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("/home/feeling/userDict.txt"), "UTF-8"));
		int counter = 0;
		for (String curDict : file.list()) {

			String curDictDir = file.getAbsolutePath() + "/" + curDict;
			System.out.println(curDictDir);
			Map<String, List<String>> curDictMap = new SougouScelReader().read(
					new DataInputStream(new FileInputStream(curDictDir)))
					.getWordMap();
			for (Entry<String, List<String>> e : curDictMap.entrySet()) {
				for (String word : e.getValue()) {
					bw.append(word.trim()).append("\n");
					counter++;
				}
			}
		}
		System.out.println("total " + counter + " words");
		bw.close();
	}

	public void removeNonsenseLongWord() throws IOException {

		Segment seg = new Segment(new DictLib());

		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("data/userDict.txt");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("/home/feeling/refineduserDict.txt"),
				"UTF-8"));

		BufferedReader br = new BufferedReader(new InputStreamReader(is,
				"UTF-8"));
		String word = null;
		int counter = 0;
		outer1: while ((word = br.readLine()) != null) {
			if(word.length() == 1){
				continue;
			}
			SegResult sr = seg.split(word);
			for (SegAtom atom : sr.getAtoms()) {
				if(atom.getWord() !=null && TermUtil.stopwords.contains(atom.getWord().trim().toLowerCase())){
					continue outer1;
				}
				
				ArrayList<Pos> posList = atom.getPosList();
				if (null != posList) {
					
					for (Pos pos : posList) {
						if (POSTag.NON_SENSE_TAG.contains(pos.getTag())) {
							continue outer1;
						}
						
					}
				}
			}
			for (SegAtom atom : sr.getAtoms()) {
				
				if (atom != null && atom.getWord() != null
						&& atom.getWord().trim().length() == 1) {
					bw.append(word).append("\n");
					counter++;
					break;
				}
			}
		}
		br.close();
		is.close();

		is = Thread.currentThread().getContextClassLoader().getResourceAsStream("data/sougouCoreDict.txt");

		br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		word = null;
		outer2: while ((word = br.readLine()) != null) {
			if(word.length() == 1){
				continue;
			}
			SegResult sr = seg.split(word);
			for (SegAtom atom : sr.getAtoms()) {
				if(atom.getWord() !=null && TermUtil.stopwords.contains(atom.getWord().trim().toLowerCase())){
					continue outer2;
				}
				
				ArrayList<Pos> posList = atom.getPosList();
				if (null != posList) {
					
					for (Pos pos : posList) {
						if (POSTag.NON_SENSE_TAG.contains(pos.getTag())) {
							continue outer2;
						}
						
					}
				}
			}
			for (SegAtom atom : sr.getAtoms()) {
				if (atom != null && atom.getWord() != null
						&& atom.getWord().trim().length() == 1) {
					bw.append(word).append("\n");
					counter++;
					break;
				}
			}
		}
		br.close();
		is.close();

		System.out.println("total " + counter + " words");

		bw.close();

	}

	public static void main(String[] args) throws IOException {
	   // new SougouScelReader().creatUserDict();
		new SougouScelReader().removeNonsenseLongWord();
	}

}
