package com.expensemanager.app.service.email;

/**
 * Created by Zhaolong Zhong on 9/1/16.
 *
 * Reference:
 * https://github.com/enrichman/androidmail
 */

public class Recipient {

    public enum TYPE { TO, CC, BCC };

    private TYPE type;
    private String address;

    public Recipient(String address) {
        this(TYPE.TO, address);
    }

    public Recipient(TYPE type, String address) {
        this.type = type;
        this.address = address;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}