package com.example.firadevianas.deteksitext;

/**
 * Created by Firadevianas on 2/11/2019.
 */

public class BudgetPlan {
    private String id,fromDate,toDate,amount,alert1,alert2,alert3;

    public BudgetPlan(){

    }

    public BudgetPlan(String id,String fromDate, String toDate, String amount,String alert1,String alert2,String alert3) {
        this.id = id;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.amount = amount;
        this.alert1=alert1;
        this.alert2=alert2;
        this.alert3=alert3;
    }

    public String getAlert1() {
        return alert1;
    }

    public void setAlert1(String alert1) {
        this.alert1 = alert1;
    }

    public String getAlert2() {
        return alert2;
    }

    public void setAlert2(String alert2) {
        this.alert2 = alert2;
    }

    public String getAlert3() {
        return alert3;
    }

    public void setAlert3(String alert3) {
        this.alert3 = alert3;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
