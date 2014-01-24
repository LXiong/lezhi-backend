package com.buzzinate.crawl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.buzzinate.crawl.core.fetch.PageFetcher;
import com.buzzinate.crawl.core.fetch.Thumbnail;
import com.buzzinate.up.UpYunClient;
import com.buzzinate.up.UpYunUtil;
import com.mortennobel.imagescaling.ResampleOp;
import com.mortennobel.imagescaling.AdvancedResizeOp;

public class ThumbnailUtil {
	private static UpYunClient upyun = new UpYunClient("lezhi", "buzzinate", "buzzinate");

	public static void main(String[] args) throws IOException {
		System.out.println(makeThumbnail("http://www.oschina.net/code/snippet_580223_14145?p=2", "http://static.oschina.net/uploads/code/201209/27140104_UeIP.jpg"));
		
	}
	
	public static String makeThumbnail(String baseUrl, String imgsrc) {
		try {
			if (imgsrc == null || !imgsrc.startsWith("http://")) return null;
			Thumbnail thumbnail = PageFetcher.fetchImage(baseUrl, imgsrc);
			BufferedImage img = thumbnail.getImage();
			if (thumbnail.getLength() > 10240 || img != null && img.getHeight() >= 200 && img.getWidth() >= 200) {
				int w = img.getWidth();
				int h = img.getHeight();
				BufferedImage cut = null;
				if (w > h) cut = img.getSubimage((w - h) / 2, 0, h, h);
				else cut = img.getSubimage(0, 0, w, w);
				ResampleOp resampleOp = new ResampleOp(200, 200);
				resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Normal);
				BufferedImage small = resampleOp.filter(cut, null);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(small, thumbnail.getFormat(), baos);
				String filename = "/lezhi_mobile/" + UpYunUtil.md5(imgsrc) + "." + thumbnail.getFormat();
				return upyun.upload(filename, baos.toByteArray(), true).url();
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}
}