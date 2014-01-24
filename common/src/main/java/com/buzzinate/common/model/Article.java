package com.buzzinate.common.model;

import static com.buzzinate.common.util.Constants.BASE_RANK_TIME;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;

@Entity(value="article", noClassnameStored=true)
public class Article {
	@Id private long id;
	
	private String url;
	// <sina short url>, without prefix http://t.cn or http://sinaurl.cn/
	private String sinaUrl;
	
	@Indexed(unique=true, dropDups=true)
	private byte[] hash;
	
	private String title;
	private String summary;
	private String fromIcon;
	private String from;
	private String content;
	private String thumbnail;
	private String charset;
		
	private String keywords;
	private int numWords;
	private float textDensity;

	private boolean isTraining;
	private Category category = Category.NONE;
	
	private String error;
	@Indexed
	private Status status;
	
	private List<Long> tweetIds;
	//微博转发数及评论数
	private long retweetSize;
	private long commentSize;
	
	//乐知用户喜欢数
	private int prefSize;
	
	// reddit's algorithm, see http://www.guwendong.com/post/2008/social_media_algorithm_reddit.html
	@Indexed
	private double score;
	
	private long createAt;
	
	public Article() {
		
	}
	
	public void updateScore() {
		// reddit's algorithm, see http://www.guwendong.com/post/2008/social_media_algorithm_reddit.html
		score = 0.0;
		double baseValue = prefSize + Math.sqrt(retweetSize);
		// 防止出现infinity
		score += baseValue > 0.0 ? Math.log10(baseValue) : 0.0;
		score += (createAt - BASE_RANK_TIME) / 36000000f;	// ms not second
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getFromIcon() {
		return fromIcon;
	}
	
	public void setFromIcon(String fromIcon) {
		this.fromIcon = fromIcon;
	}
	
	public String getFrom() {
		return from;
	}
	
	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public Category getCategory() {
		return category;
	}
	
	public void setCategory(Category category) {
		this.category = category;
	}
	
	public long getCreateAt() {
		return createAt;
	}
	
	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public int getNumWords() {
		return numWords;
	}

	public void setNumWords(int numWords) {
		this.numWords = numWords;
	}

	public float getTextDensity() {
		return textDensity;
	}

	public void setTextDensity(float textDensity) {
		this.textDensity = textDensity;
	}

	public List<Long> getTweetIds() {
		return tweetIds;
	}

	public void setTweetIds(List<Long> tweetIds) {
		this.tweetIds = tweetIds;
	}
	
	public void addTweetId(long tweetId) {
		if (tweetIds == null) tweetIds = new ArrayList<Long>();
		if (!tweetIds.contains(tweetId)) tweetIds.add(tweetId);
	}

	public long getRetweetSize() {
		return retweetSize;
	}

	public void setRetweetSize(long retweetSize) {
		this.retweetSize = retweetSize;
	}

	public long getCommentSize() {
		return commentSize;
	}

	public void setCommentSize(long commentSize) {
		this.commentSize = commentSize;
	}

	public int getPrefSize() {
		return prefSize;
	}

	public void setPrefSize(int prefSize) {
		this.prefSize = prefSize;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getSinaUrl() {
		return sinaUrl;
	}

	public void setSinaUrl(String sinaUrl) {
		this.sinaUrl = sinaUrl;
	}

	public byte[] getHash() {
		return hash;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public boolean isTraining() {
		return isTraining;
	}

	public void setTraining(boolean isTraining) {
		this.isTraining = isTraining;
	}
	
	public boolean getIsTraining() {
		return isTraining;
	}
	
	public void setIsTraining(boolean isTraining) {
		this.isTraining = isTraining;
	}
}