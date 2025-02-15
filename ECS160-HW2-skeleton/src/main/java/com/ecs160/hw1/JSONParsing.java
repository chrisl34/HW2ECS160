package com.ecs160.hw1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class JSONParsing {
    private static int counter = 1;
    private List<Post> posts = new ArrayList<>();
    public JSONParsing(){

    }

    public List<Post> getPosts(){
        return posts;
    }

    public void parse (JsonObject threadObject, int parentId, boolean firstgen, int directId) {
        //Going into Post
        int replyCount = 0;
        String createdAt = "";
        int wordCount = 0;
        JsonArray repliesArray = new JsonArray();

        if (threadObject.has("post")) {
            JsonObject postObject = threadObject.getAsJsonObject("post");
            //Going into Record
            if (postObject.has("record")) {
                JsonObject recordObject = postObject.getAsJsonObject("record");

                //Getting CreatedAt
                if (recordObject.has("createdAt")) {
                    createdAt = recordObject.get("createdAt").toString();
                }

                //Getting Text
                if (recordObject.has("text")) {
                    String text = recordObject.get("text").toString();
                    String[] words = text.trim().split("\\s+");
                    wordCount = words.length;
                }
            }
        }

        //Getting Reply Count
        if (threadObject.has("replies")) {
            repliesArray = threadObject.get("replies").getAsJsonArray();
            replyCount = repliesArray.size();
        }
        int originalId = counter;
        Post post = new Post(replyCount, createdAt, wordCount, parentId, counter, directId);
        //post.print();
        counter++;
        directId = originalId;
        if(firstgen) {
            parentId = originalId;
        }
        posts.add(post);
        //iterate through replies
        for (JsonElement feedObject : repliesArray) {
            parse(feedObject.getAsJsonObject(), parentId, false, directId);
        }
    }
}
