package com.ecs160;
import com.ecs160.persistence.Persistable;
import com.ecs160.persistence.PersistableField;
import com.ecs160.persistence.PersistableId;
import com.ecs160.persistence.PersistableListField;

@Persistable
public class Post {

    @PersistableId
    private int postId;

    @PersistableField
    private String postContent;

    @PersistableListField(className = "Post")
    private Post[] replies;

    //Constructor Function
    public Post(int postId, String postContent, Post[] replies) {
        this.postId = postId;
        this.postContent = postContent;
        this.replies = replies;
    }

    //Get postId
    public int getPostId() {
        return this.postId;
    }

    //get content
    public String getPostContent() {
        return this.postContent;
    }

    //get replies
    public Post[] getReplies() {
        return this.replies;
    }

    public void print() {
        System.out.println("Post Id: " + this.postId);
        System.out.println("Content: " + this.content);
        //System.out.println("Replies: " + Arrays.toString(this.replies));
    }
}