package com.buzzinate.vocabulary.sogou;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Sougou Pinyin IME SCEL File Reader
 * 
 * <pre>
 * SCEL Format overview:
 * 
 * General Information:
 * - Chinese characters and pinyin are all encoded with UTF-16LE.
 * - Numbers are using little endian byte order.
 * 
 * SCEL hex analysis:
 * - 0x0           Pinyin List Offset
 * - 0x120         total number of words
 * - 0x<PY-Offset> total number of pinyin
 * - ...           List of pinyin as [index, byte length of pinyin, pinyin as string] triples
 * - ...           Dictionary
 * - ...           <additional garbage>
 * 
 * Dictionary format:
 * - It can interpreted as a list of 
 *   [alternatives of words, 
 *       byte length of pinyin indexes, pinyin indexes, 
 *       [byte length of word, word as string, length of skip bytes, skip bytes]
 *       ... (alternatives) 
 *   ].
 * 
 * </pre>
 * 
 * @author keke
 */
public class SogouScelReader {
    public static void main(String[] args) throws IOException {
        // download from http://pinyin.sogou.com/dict
//        String scelFile = "X:\\kkdict\\in\\words\\sogou\\四十万汉语大词库.scel";

        // read scel into byte array
//        readWords(new File(scelFile));
    	List<String> carwords = readWords(new File("D:/dict/最新车型大全【官方推荐】.scel"));
    	List<String> fasionwords = readWords(new File("D:/dict/化妆品词汇大全.scel"));
    	String content = StringUtils.join(carwords, "\n") + "\n" + StringUtils.join(fasionwords, "\n");
    	FileOutputStream os = new FileOutputStream("D:/dict/newword.dict");
    	IOUtils.write(content, os);
    	os.close();
    }

	public static List<String> readWords(File scelFile) throws FileNotFoundException, IOException, UnsupportedEncodingException {
		List<String> words = new ArrayList<String>();
		
		ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
        FileChannel fChannel = new RandomAccessFile(scelFile, "r").getChannel();
        fChannel.transferTo(0, fChannel.size(), Channels.newChannel(dataOut));
        fChannel.close();

        // scel as bytes
        ByteBuffer dataRawBytes = ByteBuffer.wrap(dataOut.toByteArray());
        dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);

        System.out.println("文件: " + scelFile);

        byte[] buf = new byte[1024];
        String[] pyDict = new String[512];

        int totalWords = dataRawBytes.getInt(0x120);

        // pinyin offset
        dataRawBytes.position(dataRawBytes.getInt());
        int totalPinyin = dataRawBytes.getInt();
        for (int i = 0; i < totalPinyin; i++) {
            int idx = dataRawBytes.getShort();
            int len = dataRawBytes.getShort();
            dataRawBytes.get(buf, 0, len);
            pyDict[idx] = new String(buf, 0, len, "UTF-16LE");
        }

        // extract dictionary
        int counter = 0;
        for (int i = 0; i < totalWords; i++) {
            StringBuilder py = new StringBuilder();
            StringBuilder word = new StringBuilder();

            int alternatives = dataRawBytes.getShort();
            int pyLength = dataRawBytes.getShort() / 2;
            boolean first = true;
            while (pyLength-- > 0) {
                int key = dataRawBytes.getShort();
                if (first) {
                    first = false;
                } else {
                    py.append('\'');
                }
                py.append(pyDict[key]);
            }
            first = true;
            while (alternatives-- > 0) {
                if (first) {
                    first = false;
                } else {
                    word.append(", ");
                }
                int wordlength = dataRawBytes.getShort();
                dataRawBytes.get(buf, 0, wordlength);
                word.append(new String(buf, 0, wordlength, "UTF-16LE"));
                // skip bytes
                dataRawBytes.get(buf, 0, dataRawBytes.getShort());
            }
            words.add(word.toString() + "\t1");
            System.out.println(word.toString() + "\t" + py.toString());
            counter++;
        }
        System.out.println("\n读出词汇'" + scelFile + "': " + counter);
        return words;
	}
}