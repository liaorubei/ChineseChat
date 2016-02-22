package com.newclass.woyaoxue.bean;

/**
 * Created by liaorubei on 2016/2/16.
 */
public class Orders {
    public String Id;
    public String UserName;
    public String Currency;
    public double Amount;
    public double Quantity;
    public double Price;
    public String Main;
    public String Body;
    public String TradeNo;
    public String TradeStatus;
    public String LastOrderString;
    public String CreateTime;


    public Orders(double amount, String currency, String main, String body) {
        this.Amount = amount;
        this.Currency = currency;
        this.Main = main;
        this.Body = body;
    }
}

