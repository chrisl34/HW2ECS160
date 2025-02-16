package com.ecs160;
import com.ecs160.persistence.Persistable;
import com.ecs160.persistence.PersistableField;
import com.ecs160.persistence.PersistableId;
import com.ecs160.persistence.PersistableListField;
import java.util.List;

@Persistable
public class Post {

    @PersistableId
    private Integer postId;

    @PersistableField
    private String postContent;

    @PersistableListField(className = "com.ecs160.Post")
    private List<Post> replies;

    public Post(){

    }

    //Constructor Function
    public Post(int postId, String postContent, List<Post> replies) {
        this.postId = postId;
        this.postContent = postContent;
        this.replies = replies;
    }

    //Get postId
    public Integer getPostId() {
        return this.postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public void setReplies(List<Post> replies) {
        this.replies = replies;
    }

    //get content
    public String getPostContent() {
        return this.postContent;
    }

    //get replies
    public List<Post> getReplies() {
        return this.replies;
    }

    public void printTest() {
        System.out.println("Post Id: " + this.postId);
        System.out.println("Content: " + this.postContent);
        for(Post reply : this.replies) {
            System.out.println("Reply");
            reply.printTest();
        }
    }

    public void printToUser(){
        System.out.println(this.postContent);
        for(Post reply : this.replies) {
            System.out.println("-->" + reply.getPostContent());
        }
    }
}