package com.buzzinate.crawl.core.fetch;

import java.awt.image.BufferedImage;

public class Thumbnail {
	private BufferedImage image;
	private String format;
	private long length;
	
	public Thumbnail(BufferedImage image, String format, long length) {
		this.image = image;
		this.format = format;
		this.length = length;
	}

	public BufferedImage getImage() {
		return image;
	}

	public String getFormat() {
		return format;
	}

	public long getLength() {
		return length;
	}
}