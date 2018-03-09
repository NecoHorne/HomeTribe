package com.necohorne.hometribe.Models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by necoh on 2018/03/01.
 * camelCase will not be used in the naming of the fields of this class as it may lead to issues down
 * the line with the Firebase database. Use all lowercase letters, words can be separated by an underscore.
 */

public class ChatMessage {

    private String message;
    private String user_id;
    private String timestamp;
    private String name;
    private LatLng message_location;
    private String key_ref;

    public ChatMessage() {
    }

    public ChatMessage(String message, String user_id, String timestamp, String name, LatLng message_location, String key_ref) {
        this.message = message;
        this.user_id = user_id;
        this.timestamp = timestamp;
        this.name = name;
        this.message_location = message_location;
        this.key_ref = key_ref;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getMessage_location() {
        return message_location;
    }

    public void setMessage_location(LatLng message_location) {
        this.message_location = message_location;
    }

    public String getKey_ref() {
        return key_ref;
    }

    public void setKey_ref(String key_ref) {
        this.key_ref = key_ref;
    }
}
