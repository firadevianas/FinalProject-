package com.example.firadevianas.deteksitext;

/**
 * Created by Firadevianas on 2/11/2019.
 */

public class Transaction {
    private String id,date,amount,desc,category,title;

    public Transaction() {

    }
    public Transaction(String id,String date, String amount, String desc, String category,String title) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.desc = desc;
        this.title = title;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
