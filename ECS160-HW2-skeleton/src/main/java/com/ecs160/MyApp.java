package com.ecs160;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonDeserializer;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.List;
import com.ecs160.persistence.*;

public class MyApp {
    public static void main(String[] args) throws FileNotFoundException, URISyntaxException, NoSuchFieldException {
        String inputFile = "input.json";
        Parser p = new Parser();
        JsonElement element = JsonParser.parseReader(new InputStreamReader(JsonDeserializer.class.getClassLoader().getResourceAsStream(inputFile)));
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray feedArray = jsonObject.get("feed").getAsJsonArray();

            for (JsonElement feedObject : feedArray) {
                // Check if you have the thread key
                if (feedObject.getAsJsonObject().has("thread")) {
                    JsonObject threadObject = feedObject.getAsJsonObject().get("thread").getAsJsonObject();
                    p.parse(threadObject,true);
                }
            }
        }

        List<Post> posts = p.getPosts();
        Session session = new Session();
        for (Post post : posts) {
            session.add(post);
        }
        session.persistAll();
        Post t1 = new Post();
        t1.setPostId(1);
        Post test = (Post)session.load(t1);
        test.print();

    }
}
