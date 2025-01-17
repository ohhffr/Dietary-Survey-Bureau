package com.example.quantacup.bean;

public class SubComment {
    private int subCommentId;
    private int commentId;
    private String userId;
    private String userName;
    private String content;
    private String createdAt;
    private String userAvatarURL; // 添加头像URL字段

    public SubComment(String userId, String userName, String content, String createdAt, String userAvatarURL) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
        this.userAvatarURL = userAvatarURL;
    }

    public SubComment(int subCommentId, String userId, String userName, String content, String createdAt, String userAvatarURL) {
        this.subCommentId = subCommentId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
        this.userAvatarURL = userAvatarURL;
    }

    public int getSubCommentId() {
        return subCommentId;
    }

    public void setSubCommentId(int subCommentId) {
        this.subCommentId = subCommentId;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserAvatarURL() {
        return userAvatarURL;
    }

    public void setUserAvatarURL(String userAvatarURL) {
        this.userAvatarURL = userAvatarURL;
    }
}
