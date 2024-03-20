package com.qianye.youyuan.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {
    //
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
//        valueOperations.set("qianyeString", "dog");
//        valueOperations.set("qianyeInt", 1);
//        valueOperations.set("qianyeDouble", 2.0);
//        User user = new User();
//        user.setId(1L);
//        user.setUsername("qianye");
//        valueOperations.set("qianyeUser", user);
//        //查
//        Object qianye = valueOperations.get("qianyeString");
//        Assertions.assertTrue("dog".equals((String) qianye));
//        qianye = valueOperations.get("qianyeInt");
//        Assertions.assertTrue(1 == ((Integer) qianye));
//        qianye = valueOperations.get("qianyeDouble");
//        Assertions.assertTrue(2.0 == ((Double) qianye));
//        System.out.println(valueOperations.get("qianyeUser"));
//        valueOperations.set("qianyeString","dog");
        redisTemplate.delete("qianyeString");
    }
}