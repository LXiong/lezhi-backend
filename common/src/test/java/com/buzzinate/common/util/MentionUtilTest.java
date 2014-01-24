package com.buzzinate.common.util;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class MentionUtilTest {
	
	@Test
	public  void testParseMentions() {
		String text = "@张三 @李四//@Jason，我的注释";
		
		List<String> mentions = MentionUtil.parseMentions(text);
		assertEquals(new String[]{"张三", "李四", "Jason"}, mentions.toArray(new String[0]));
	}
	
	@Test
	public  void testParseSlashMentions() {
		String text = "@张三 @李四//@Jason，我的注释";
		
		List<String> mentions = MentionUtil.parseSlashMentions(text);
		assertEquals(new String[]{"Jason"}, mentions.toArray(new String[0]));
	}

	public static void assertEquals(String[] expected, String[] actual) {
		Assert.assertEquals(expected.length, actual.length);
		for(int i = 0; i < expected.length; i++) {
			Assert.assertEquals(expected[i], actual[i]);
		}
	}
}
