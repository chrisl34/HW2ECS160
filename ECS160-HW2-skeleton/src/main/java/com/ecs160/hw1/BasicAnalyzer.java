package com.ecs160.hw1;

import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class BasicAnalyzer {
    private boolean weights;

    public BasicAnalyzer(boolean w) {
        this.weights = w;
    }
    public static ArrayList<String> checkRedisData(Jedis jedis, boolean weights, double longestText) {
        Set<String> keys = jedis.keys("*");  // Get all keys in Redis
        int totalPosts = keys.size();
        double kidsWT = 0;
        double parentsWT = 0;
        int kids = 0;
        double parents = 0;
        Duration interval = Duration.ZERO;
        int c = 50;

        int maxWordCount = Integer.MIN_VALUE; // Initialize to minimum integer value

        for (Map.Entry<Integer, Post> entry : input.entrySet()) {
            Integer key = entry.getKey();
            Post post = entry.getValue();
            // Update maxWordCount if the current value is greater
            if (post.getWordCount() > maxWordCount) {
                maxWordCount = post.getWordCount();
            }
        }
        double longestText = maxWordCount;
        for (Map.Entry<Integer, Post> entry : input.entrySet()) {
            Integer key = entry.getKey(); // The key (e.g., Redis key as an integer)
            Post post = entry.getValue(); // The corresponding Post object
            if (!weights) {
                if (post.getParentId() == -1) {
                    parents++;
                } else {
                    kids++;
                    interval = interval.plus(compareTimes(post.getCreatedAt(), input.get(post.getDirect()).getCreatedAt()));

                }
            } else {
                if (post.getParentId() == -1) {
                    parentsWT += (1 + (post.getWordCount() / longestText));
                    parents++;
                } else {
                    kids++;
                    kidsWT += (1 + (post.getWordCount() / longestText));
                    interval = interval.plus(compareTimes(post.getCreatedAt(), input.get(post.getDirect()).getCreatedAt()));
                }

            }
        }

        if(weights) {
            System.out.println("Total posts: " + (int)(kidsWT + parentsWT));
            System.out.println("Average number of replies: " + (int)(kidsWT/parents));
        } else {
            System.out.println("Total posts: " + (int) (kids + parents));
            System.out.println("Average number of replies: " + (int) (kids / parents));
        }
        //Average out the duration
        interval = interval.dividedBy(kids);
        // Convert to hours, minutes, and seconds
        long hours = interval.toHours();
        long minutes = interval.toMinutesPart();
        long seconds = interval.toSecondsPart();
        // Format as HH:mm:ss
        String formattedDiff = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        System.out.println("Average duration between replies: " + formattedDiff);
        ArrayList<String> l = new ArrayList<>();
        if(!weights){
            l.add(Integer.toString((int)(kids + parents)));
            l.add(Integer.toString((int)(kids/parents)));
            l.add(formattedDiff);
        }else{
            l.add(Integer.toString((int)(kidsWT + parentsWT)));
            l.add(Integer.toString((int)(kidsWT/parents)));
            l.add(formattedDiff);
        }
        return l;
    }
    public static Duration compareTimes(String t1, String t2) {
        t1 = t1.replace("\"", "").trim();
        t2 = t2.replace("\"", "").trim();

        // Convert strings to ZonedDateTime
        ZonedDateTime dateTime1 = ZonedDateTime.parse(t1, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        ZonedDateTime dateTime2 = ZonedDateTime.parse(t2, DateTimeFormatter.ISO_ZONED_DATE_TIME);

        // Calculate time difference
        Duration duration = Duration.between(dateTime2, dateTime1);

        return duration;
    }
}
