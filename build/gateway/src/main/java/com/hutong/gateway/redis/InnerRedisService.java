package com.hutong.gateway.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.hutong.framework.util.RedisServiceBase;

/**
 * Redis工具类
 * 
 */
@Service
public class InnerRedisService extends RedisServiceBase {

	protected RedisTemplate<Object, Object> redisTemplate;

	public void initInnerRedisGateway(RedisTemplate<Object, Object> redisTemplate){
		this.redisTemplate = redisTemplate;
	}
	
	public RedisTemplate<Object, Object> getRedisTemplate() {
		return redisTemplate;
	}
}
