package com.tzc.socket.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tongzhichao on 17-5-10.
 */

public class Message {

    public static final int MESSAGE = 0, FEEDBACK = 1, JOIN = 2;
    private String msg;
    private int time;
    private int type;
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }


    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public static String pack(String msg) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", MESSAGE);
            jsonObject.put("msg", msg);
            jsonObject.put("time", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static String feedback(int time) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", FEEDBACK);
            jsonObject.put("time", time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static Message getMessage(String msg) {
        try {
            JSONObject jsonObject = new JSONObject(msg);
            Message message = new Message();
            message.type = jsonObject.getInt("type");
            message.time = jsonObject.getInt("time");
            if (message.type == MESSAGE) {
                message.msg = jsonObject.getString("msg");
            }
            return message;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
