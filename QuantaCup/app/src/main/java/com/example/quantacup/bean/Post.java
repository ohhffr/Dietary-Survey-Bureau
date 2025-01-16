package com.example.quantacup.bean;

import java.util.List;

public class Post {
    private String userId;
    private int noteId;
    private String userAvatarUrl; // 用户头像的 URL
    private String username;       // 用户名
    private String postTime;       // 发表时间
    private String content;        // 帖子内容
    private List<String> imageUrls; // 可能的图片 URL 列表

    private boolean collected; // 收藏状态


    public Post(String userId, int noteId, String userAvatarUrl, String username, String postTime, String content, List<String> imageUrls) {
        this.userId = userId;
        this.noteId = noteId;
        this.userAvatarUrl = userAvatarUrl;
        this.username = username;
        this.postTime = postTime;
        this.content = content;
        this.imageUrls = imageUrls;
    }

    // Getter 和 Setter 方法
    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }
}
