package com.buzzinate.jianghu.jredis;

import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ShardedJedis;

public class ShardedPipeline {
	private ShardedJedis shardedJedis;
	private Map<JedisShardInfo, Pipeline> si2ps = new HashMap<JedisShardInfo, Pipeline>();
	
	public ShardedPipeline(ShardedJedis shardedJedis) {
		this.shardedJedis = shardedJedis;
	}
	
	public Pipeline getPipeline(String key) {
		JedisShardInfo si = shardedJedis.getShardInfo(key);
		Pipeline p = si2ps.get(si);
		if (p == null) {
			p = shardedJedis.getShard(key).pipelined();
			si2ps.put(si, p);
		}
		return p;
	}
	
	public void sync() {
		for (Pipeline p: si2ps.values()) p.sync();
	}
}