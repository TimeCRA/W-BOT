package top.lsyweb.qqbot.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.lsyweb.qqbot.exception.ServiceException;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 写入缓存，永不过期
     *
     * @param key   String
     * @param value Object
     * @return flag boolean
     */
    public boolean set(String key, Object value) {
        boolean flag = false;
        try {
            redisTemplate.opsForValue().set(key, value);
            flag = true;
        } catch (Exception ex) {
            flag = false;
        }
        return flag;
    }

    public void leftPush(String key, String value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception ex) {
            throw new ServiceException("redis插入队列失败");
        }
    }

    public String leftPop(String key) {
        try {
            return (String) redisTemplate.opsForList().leftPop(key);
        } catch (Exception ex) {
            throw new ServiceException("redis弹出队列失败");
        }
    }

    public void rightPush(String key, String value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception ex) {
            throw new ServiceException("redis插入队列失败");
        }
    }

    public String rightPop(String key) {
        try {
            return (String) redisTemplate.opsForList().rightPop(key);
        } catch (Exception ex) {
            throw new ServiceException("redis弹出队列失败");
        }
    }

    public Long getListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception ex) {
            throw new ServiceException("redis获取队列长度失败");
        }
    }

    /**
     * 设置带过期时间的缓存
     *
     * @param key    String
     * @param value  Object
     * @param expire long 过期时间
     * @return flag boolean
     */
    public boolean set(String key, Object value, long expire) {
        boolean flag = false;
        try {
            if (expire > 0) {
                redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
            } else {
                this.set(key, value);
            }
            flag = true;
        } catch (Exception ex) {
            flag = false;
        }
        return flag;
    }

    /**
     * 取值方法
     *
     * @param key
     * @return
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Integer getInt(String key) {
        Object o = get(key);
        return o == null ? null : (Integer) o;
    }

    public Integer getIntOrDefault(String key, Integer def) {
        Object o = get(key);
        return o == null ? def : (Integer) o;
    }

    /**
     * 删除方法
     *
     * @param key
     * @return
     */
    public boolean del(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断某个key是否存在
     *
     * @param key
     * @return
     */
    public boolean exist(String key) {
        Boolean flag = false;
        try {
            flag = redisTemplate.hasKey(key);
            return flag;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 查找匹配的key值，返回一个Set集合类型
     *
     * @param name
     * @return
     */
    public Set<String> getKeys(String name) {
        Set<String> keys = redisTemplate.keys("*");
        return keys;
    }

    /**
     * 从redis中获取key对应的过期时间;
     * 如果该值有过期时间，就返回相应的过期时间;
     * 如果该值没有设置过期时间，就返回-1;
     * 如果没有该key，就返回-2;
     *
     * @param key
     * @return
     */
    public long expire(String key) {
        return redisTemplate.opsForValue().getOperations().getExpire(key);
    }

    /**
     * @param key   :	redis 的键
     * @param start :	 修剪开始位置
     * @param end   ：修建结束位置
     * @description:
     * @return: null
     * @author: jiangzhiwen
     * @date: 2022-01-20 10:34:01
     */
    public void ltrim(String key, Long start, Long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

}

