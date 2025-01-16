package com.example.quantacup.bean;

import java.util.List;

public class Comment {
    private int commentId;
    private int noteId;
    private String userId;
    private String userName;
    private String userAvatarUrl; // 用户头像的 URL
    private String content;
    private String createdAt;
    private List<SubComment> subComments;

    public Comment(String userId, int noteId, String userAvatarUrl, String userName, String createdAt, String content, List<SubComment> subComments) {
        this.userId = userId;
        this.noteId = noteId;
        this.userAvatarUrl = userAvatarUrl;
        this.userName = userName;
        this.createdAt = createdAt;
        this.content = content;
        this.subComments = subComments;
    }


    public Comment(String userId,int noteId, String userAvatarUrl, String userName, String createdAt, String content) {
        this.noteId = noteId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatarUrl = userAvatarUrl;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Comment(int commentId, int noteId, String userId, String userName, String userAvatarUrl, String content, String createdAt, List<SubComment> subComments) {
        this.commentId = commentId;
        this.noteId = noteId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatarUrl = userAvatarUrl;
        this.content = content;
        this.createdAt = createdAt;
        this.subComments = subComments;
    }

    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
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

    public List<SubComment> getSubComments() {
        return subComments;
    }

    public void setSubComments(List<SubComment> subComments) {
        this.subComments = subComments;
    }
}
