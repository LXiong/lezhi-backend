package com.buzzinate.common.util;

import java.util.Comparator;

import com.buzzinate.common.message.RepostMessage;

public class RepostMsgComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		RepostMessage msg0 = (RepostMessage) arg0;
		RepostMessage msg1 = (RepostMessage) arg1;

		if (msg0.repostCount > msg1.repostCount) {
			return -1;
		} else if (msg0.repostCount < msg1.repostCount) {
			return 1;
		} else {
			return 0;
		}
	}
}