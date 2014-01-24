package com.buzzinate.crawl.core.fetch;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaCharsetDetector {
	private static final Pattern META_CONTENT_TYPE_PATTERN = Pattern.compile("(?is)<meta\\s+http-equiv\\s*=\\s*['\\\"]*\\s*content-type['\\\"]*\\s+content\\s*=\\s*['\\\"]([^'\"]+)['\\\"]");
    private static final Pattern CHARSET_PATTERN = Pattern.compile("(?is)<meta\\s+charset\\s*=\\s*['\\\"]([^'\"]+)['\\\"]");
    
    public static String detectMetaCharset(byte[] bs) throws UnsupportedEncodingException{
	    String html = new String(Arrays.copyOf(bs, 10240),  "UTF-8");

	    Matcher m = META_CONTENT_TYPE_PATTERN.matcher(html);
	    if (m.find()) {
	      String[] directives = m.group(1).toLowerCase().split(";");
	      
	      for (String directive : directives) {
	        String trimdirective = directive.trim();
	        if (trimdirective.startsWith("charset=")) return trimdirective.substring("charset=".length());
	      }
	    }
	    
	    Matcher cm = CHARSET_PATTERN.matcher(html);
	    if (cm.find()) {
	      return cm.group(1).toLowerCase();
	    }
	    return null;
    }
}
