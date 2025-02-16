package com.ecs160;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private int counter = 1;
    private List<Post> posts = new ArrayList<>();
    public Parser(){}

    public List<Post> getPosts(){
        return posts;
    }
    public Post parse (JsonObject threadObject, boolean firstgen) {
        JsonArray repliesArray = new JsonArray();
        String text = "";
        if (threadObject.has("post")) {
            JsonObject postObject = threadObject.getAsJsonObject("post");
            //Going into Record
            if (postObject.has("record")) {
                JsonObject recordObject = postObject.getAsJsonObject("record");

                //Getting Text
                if (recordObject.has("text")) {
                    text = recordObject.get("text").toString();
                }
            }
        }
        List<Post> replies = new ArrayList<>();
        //Getting Reply Count
        if(firstgen) {
            if (threadObject.has("replies")) {
                repliesArray = threadObject.get("replies").getAsJsonArray();
                for(JsonElement reply : repliesArray) {
                    replies.add(parse(reply.getAsJsonObject(), false));
                }
            }
        }
        Post post = new Post(counter, text, replies);
        counter++;
        posts.add(post);
        return post;
    }
}