package com.ryanchan.chatroom;

/**
 * Created by ryanchan on 7/8/16.
 */
public class Chat {

    private String message;
    private String author;

    @SuppressWarnings("unused")
    private Chat() {
    }

    Chat(String message, String author) {
        this.message = message;
        this.author = author;
    }

    public String getMessage() {
        return message;
    }
    public String getAuthor() {
        return author;
    }
}
