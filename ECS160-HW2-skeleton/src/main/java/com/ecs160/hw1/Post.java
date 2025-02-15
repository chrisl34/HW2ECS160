package com.ecs160.hw1;

public class Post {

    private int replyCount;
    private String createdAt;
    private int wordCount;
    private int parentId;
    private int id;
    private int direct;

    //Constructor Function
    public Post(int replyCount, String createdAt, int wordCount, int parentId, int id, int direct) {
        this.replyCount = replyCount;
        this.createdAt = createdAt;
        this.wordCount = wordCount;
        this.parentId = parentId;
        this.id = id;
        this.direct = direct;
    }
    //Get reply count
    public int getReplyCount() {
        return this.replyCount;
    }

    //get created at
    public String getCreatedAt() {
        return this.createdAt;
    }

    //get text
    public int getWordCount() {
        return this.wordCount;
    }

    //get parentId
    public int getParentId() {
        return this.parentId;
    }

    //get id
    public int getId() {
        return this.id;
    }

    public int getDirect(){
        return this.direct;
    }

    public void print() {
        System.out.println("Reply Count: " + this.replyCount);
        System.out.println("Created At: " + this.createdAt);
        System.out.println("Word Count: " + this.wordCount);
        System.out.println("Parent Id: " + this.parentId);
        System.out.println("Direct Id: " + this.direct);
        System.out.println("Id: " + this.id);
    }
}