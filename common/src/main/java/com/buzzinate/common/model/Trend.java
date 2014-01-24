package com.buzzinate.common.model;

import static com.buzzinate.common.util.Constants.BASE_RANK_TIME;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

@Entity(value = "trend", noClassnameStored = true)
public class Trend {

	@Id
	private Long id;

	private String name;

	@Indexed(unique = true, dropDups = true)
	private byte[] hash;

	@Indexed(background = true, value = IndexDirection.DESC)
	private Long count;

//	@Indexed
//	private double score;

	@Indexed(background = true)
	private Long createAt;

	public Trend() {
		super();
//		this.score = 0.0;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getHash() {
		return hash;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

//	public double getScore() {
//		return score;
//	}
//
//	public void updateScore(Long retweetSize) {
//		// reddit's algorithm, see
//		// http://www.guwendong.com/post/2008/social_media_algorithm_reddit.html
//		double baseValue = Math.sqrt(retweetSize);
//		Boolean isNewTrend = false;
//		if (score < 10.0) {
//			isNewTrend = true;
//		}
//		// 防止出现infinity
//		score += baseValue > 0.0 ? Math.log10(baseValue) : 0.0;
//		score += (System.currentTimeMillis() - BASE_RANK_TIME) / 36000000f; // ms not second
//		if(!isNewTrend){
//			score /= 2.0;
//		}
//	}

	public Long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Long createAt) {
		this.createAt = createAt;
	}

}
