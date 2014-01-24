package com.buzzinate.util;

import com.buzzinate.common.util.Constants;
import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;

public class LanguageDetector {
	public static boolean isTargetLanguage(String url, String title) {

		Detector detector = DetectorFactory.create();
		detector.append(title);
		try {
			String lang = detector.detect();
			return !Constants.FILT_LANGUAGES.contains(lang);
		} catch (Exception e) {
			// 如果我们不能正确识别，则默认为目标语言
			return true;
		}
	}
}
