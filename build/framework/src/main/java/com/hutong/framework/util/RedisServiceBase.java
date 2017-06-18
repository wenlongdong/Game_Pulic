package com.hutong.framework.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

public abstract class RedisServiceBase {

	public abstract RedisTemplate<Object, Object> getRedisTemplate();

	public boolean exist(String key) {
		return getRedisTemplate().hasKey(key);
	}

	public void del(String key) {
		getRedisTemplate().delete(key);
	}

	public Boolean expire(String key, long timeout, TimeUnit unit) {
		return getRedisTemplate().expire(key, timeout, unit);
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------------------String操作
	public void valueSet(String key, Object object) {
		String v = "{}";
		if (!(object instanceof String)) {
			v = UtilJson.O2S(object);
		} else {
			v = object.toString();
		}
		getRedisTemplate().opsForValue().set(key, v);
	}

	@SuppressWarnings("unchecked")
	public <T> T valueGet(String key, Class<T> clazz) {
		Object object = getRedisTemplate().opsForValue().get(key);
		if (null == object) {
			return null;
		}
		if (clazz.equals(String.class)) {
			return (T) object;
		} else {
			return (T) UtilJson.S2O(object.toString(), clazz);
		}
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------Hash操作
	public void hashSet(String key, String hash, Object o) {
		HashOperations<Object, Object, Object> hashOperations = getRedisTemplate().opsForHash();
		if (o instanceof String) {
			hashOperations.put(key, hash, o);
		} else {
			hashOperations.put(key, hash, UtilJson.O2S(o));
		}
	}
	
	/**
	 * 批量存入hash，map内容为要存入的hash键值对
	 * @param key
	 * @param map
	 */
	public void hashSetAll(String key, Map<String, String> map) {
		HashOperations<Object, Object, Object> hashOperations = getRedisTemplate().opsForHash();
		hashOperations.putAll(key, map);
	}

	@SuppressWarnings("unchecked")
	public <T> T hashGet(String key, String hash, Class<T> clazz) {
		HashOperations<Object, Object, Object> hashOperations = getRedisTemplate().opsForHash();
		Object object = hashOperations.get(key, hash);
		if (null == object) {
			return null;
		}
		if (clazz.equals(String.class)) {
			return (T) object;
		} else {
			return (T) UtilJson.S2O(object.toString(), clazz);
		}
	}
	
	public void hashDel(String key, String hash) {
		HashOperations<Object, Object, Object> hashOperations = getRedisTemplate().opsForHash();
		hashOperations.delete(key, hash);
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> hashValues(String key, Class<T> clazz) {
		HashOperations<Object, Object, Object> opsForHash = getRedisTemplate().opsForHash();
		List<Object> values = opsForHash.values(key);
		List<T> vs = new ArrayList<T>();
		for (Object object : values) {
			if (clazz.equals(String.class)) {
				vs.add((T) object.toString());
			} else {
				vs.add(UtilJson.S2O(object.toString(), clazz));
			}
		}
		return vs;
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------sorted set操作
	public void zAdd(String key, Object o, double score) {
		ZSetOperations<Object, Object> opsForZSet = getRedisTemplate().opsForZSet();
		if (o instanceof String) {
			opsForZSet.add(key, o, score);
		} else {
			opsForZSet.add(key, UtilJson.O2S(o), score);
		}
	}
	
	public void zRem(String key, Object o) {
		ZSetOperations<Object, Object> opsForZSet = getRedisTemplate().opsForZSet();
		if (o instanceof String) {
			opsForZSet.remove(key, o);
		} else {
			opsForZSet.remove(key, UtilJson.O2S(o));
		}
	}
	
	//注意 排序返回从0开始  按照score从小到大排序
	public long zRank(String key, Object o) {
		ZSetOperations<Object, Object> opsForZSet = getRedisTemplate().opsForZSet();
		if (o instanceof String) {
			return opsForZSet.rank(key, o);
		} else {
			return opsForZSet.rank(key, UtilJson.O2S(o));
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> Set<T> zGetTopASC(String key, int top, Class<T> clazz) {
		ZSetOperations<Object, Object> opsForZSet = getRedisTemplate().opsForZSet();
		Set<Object> range = opsForZSet.range(key, 0, top);
		Set<T> r = new HashSet<T>();
		if (clazz.equals(String.class)) {
			for (Object o : range) {
				r.add((T) o.toString());
			}
		} else {
			for (Object o : range) {
				r.add(UtilJson.S2O(o.toString(), clazz));
			}
		}
		return r;
	}
	
	public <T> Long zGetRevRankByValue(String key, Object value, Class<T> clazz) {
		ZSetOperations<Object, Object> opsForZSet = getRedisTemplate().opsForZSet();
		if (clazz.equals(String.class)) {
			return opsForZSet.reverseRank(key, value);
		} else {
			return opsForZSet.reverseRank(key, UtilJson.O2S(value));
		}
	}
	
	public <T> Double zGetScoreByValue(String key, Object value, Class<T> clazz) {
		ZSetOperations<Object, Object> opsForZSet = getRedisTemplate().opsForZSet();
		if (clazz.equals(String.class)) {
			return opsForZSet.score(key, value);
		} else {
			return opsForZSet.score(key, UtilJson.O2S(value));
		}
	}
	
	public Long zSize(String key) throws Exception {
		ZSetOperations<Object, Object> opsForZSet = getRedisTemplate().opsForZSet();
		return opsForZSet.size(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Set<T> zGetByScore(String key, long min, long max, Class<T> clazz) {
		ZSetOperations<Object, Object> opsForZSet = getRedisTemplate().opsForZSet();
		Set<Object> rangeByScore = opsForZSet.rangeByScore(key, min, max);
		Set<T> r = new HashSet<T>();
		if (clazz.equals(String.class)) {
			for (Object o : rangeByScore) {
				r.add((T) o.toString());
			}
		} else {
			for (Object o : rangeByScore) {
				r.add(UtilJson.S2O(o.toString(), clazz));
			}
		}
		return r;
	}
	
	// ---------------------------------------------------------------------------------------------------------------------------------------list操作
	
	
}
