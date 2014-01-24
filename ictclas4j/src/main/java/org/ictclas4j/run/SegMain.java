package org.ictclas4j.run;

import java.util.List;

import org.ictclas4j.bean.DictLib;
import org.ictclas4j.bean.Pos;
import org.ictclas4j.bean.SegResult;
import org.ictclas4j.segment.Segment;


/**
 * Copyright 2007.6.1 张新波（sinboy）
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

public class SegMain {
	public static Segment seg = new Segment(new DictLib(false));

	public static void main(String[] args) {		 
		String[] source = { 
				"本次北京车展上，英菲尼迪将推出一款全球首发，专门针对中国消费者需求而打造的混合动力轿车长轴距版----M35hL，这是一款英菲尼迪首次专为中国市场打造的全新车型。",
				"程序员不是一般的人",
				"行星边际2独享九城CJ展台 或将于下月测试",
				"正如第一名的的营销学家菲利普·科特勒在70年代指出的“谁拥有了绿色产品，谁将拥有市场”。",
				"大连银行便是其中的佼佼者。", 
				"2006年德国世界杯八分之一决赛马上要开始",
				"广州：网络警察“巡逻”BBS和博客",
				"18时42分的球赛就要开始了，在南方公园",
				"张新波住在雅仕苑",
				"张新波在杭州",
				"红眼病很多",
				"中国人民从此站了起来,我发财了",
				"年发飙",
				"邓颖超和周恩来",
				"深圳市月份牌，我發财了",
				"走遍撒哈拉 寻找三毛足迹·沙沙&刀子19天9千元穷游摩洛哥&西撒哈拉游记",
				"三星ＳＨＭ－１００型电视获得了工业设计大奖"};
		String prev = null;
		for (int k = 0; k < Integer.MAX_VALUE; k++) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < source.length; i++) {
				long start = System.currentTimeMillis();
				SegResult sr = seg.split(source[i]);
				long end = System.currentTimeMillis();
				String r = sr.toString();
				System.out.println("time:" + (end-start) + " " + r);
				sb.append(r);
			}
			String now = sb.toString();
			System.out.println();
			if (prev != null && !now.equals(prev)) {
				System.out.println(k + "iteration");
			}
			prev = now;
		}
	}

}
