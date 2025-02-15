package com.ecs160.hw1;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class RedisDB {
    static Jedis jedisSess;
    static {
        jedisSess = new Jedis("localhost", 6379);
    }

    public void persistAll(List<Post> p){
        for(Post post: p){
            Map<String, String> postMap = new HashMap<>();
            postMap.put("rootID", Integer.toString(post.getParentId()));
            postMap.put("directParentID", Integer.toString(post.getDirect()));
            postMap.put("replyCount", Integer.toString(post.getReplyCount()));
            postMap.put("wordCount", Integer.toString(post.getWordCount()));
            postMap.put("createdAt", post.getCreatedAt());
            jedisSess.hset(Integer.toString(post.getId()), postMap);
        }
    }

    public Map<Integer, Post> readFromDB(){
        Set<String> keys = jedisSess.keys("*");  // Get all keys in Redis
        Map<Integer, Post> postsMap = new HashMap<>(); // Map to store posts by key

        for (String key : keys) {
            Map<String, String> postMap = jedisSess.hgetAll(key);
            Post p = new Post(
                    Integer.parseInt(postMap.get("replyCount")),
                    postMap.get("createdAt"),
                    Integer.parseInt(postMap.get("wordCount")),
                    Integer.parseInt(postMap.get("rootID")),
                    Integer.parseInt(key),
                    Integer.parseInt(postMap.get("directParentID"))
            );
            postsMap.put(Integer.parseInt(key), p); // Use the integer key for the map
        }

        return postsMap;
    }
}
