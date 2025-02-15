//Chris Lo
//Aadhi Aravind

package com.ecs160.hw1;

import java.io.*;
import java.util.List;

import org.apache.commons.cli.*;
//Imports for GSON
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonDeserializer;

import java.util.Map;
//Imports for time comparison

//Import for Date Time difference


public class SocialMediaAnalyzerDriver {
    static int counter = 1;
    static double longestText = 0;

    public static void main(String[] args) throws ParseException {
        Options opt = new Options();
        opt.addOption(Option.builder("w")
                        .longOpt("weighted")
                .hasArg(true)
                .desc("Description of the weighted option")
                .build());

        opt.addOption(Option.builder("f")
                .longOpt("file")
                .hasArg(true)
                .desc("Input file or not")
                .build());
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( opt, args);

        String inputFile = "input.json";
        if(cmd.hasOption("f")) {
            inputFile = cmd.getOptionValue("f");
            fileGiven = true;
        }
        if(fileGiven){
            //JsonElement element = JsonParser.parseReader(new InputStreamReader(JsonDeserializer.class.getClassLoader().getResourceAsStream(inputFile)));
            JsonElement element = JsonParser.parseReader(new InputStreamReader(new FileInputStream(inputFile)));
            if (element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();

                JsonArray feedArray = jsonObject.get("feed").getAsJsonArray();

                for (JsonElement feedObject : feedArray) {
                    // Check if you have the thread key
                    if (feedObject.getAsJsonObject().has("thread")) {
                        JsonObject threadObject = feedObject.getAsJsonObject().get("thread").getAsJsonObject();
                        j.parse(threadObject, -1, true, -1);
                    }
                }
            }
        }else if(!fileGiven){
            JsonElement element = JsonParser.parseReader(new InputStreamReader(JsonDeserializer.class.getClassLoader().getResourceAsStream(inputFile)));
            if (element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();

                JsonArray feedArray = jsonObject.get("feed").getAsJsonArray();

                for (JsonElement feedObject : feedArray) {
                    // Check if you have the thread key
                    if (feedObject.getAsJsonObject().has("thread")) {
                        JsonObject threadObject = feedObject.getAsJsonObject().get("thread").getAsJsonObject();
                        j.parse(threadObject, -1, true, -1);
                    }
                }
            }
        }
        List<Post> p = j.getPosts();
        RedisDB jedis = new RedisDB();
        jedis.persistAll(p);
        Map<Integer, Post> postsDB = jedis.readFromDB();
        BasicAnalyzer ba = new BasicAnalyzer(Boolean.parseBoolean(cmd.getOptionValue("weighted", "false")));
        ba.calculateStatistics(postsDB);
        }
    }
