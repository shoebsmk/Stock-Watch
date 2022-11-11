package com.example.stockwatch;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;

public class Stock implements Comparable<Stock>{

    private static final DecimalFormat df = new DecimalFormat("0.00");

    private String stockSymbol;
    private String companyName;
    private double currentPrice;
    private double priceChange;
    private double changePercent;

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setPriceChange(double priceChange) {
        this.priceChange = priceChange;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getPriceChange() {
        return priceChange;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public Stock(String stockSymbol, String companyName, double currentPrice, double priceChange, double changePercent) {
        this.stockSymbol = stockSymbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.priceChange = Double.parseDouble(df.format(priceChange));
        this.changePercent = Double.parseDouble(df.format(changePercent));
    }
    public JSONObject toJSON() throws JSONException {
        JSONObject stockJSON = new JSONObject();
        stockJSON.put("stockSymbol",stockSymbol);
        stockJSON.put("companyName", companyName);
//        stockJSON.put("currentPrice", currentPrice);
//        stockJSON.put("priceChange", priceChange);
//        stockJSON.put("changePercent", changePercent);
        return stockJSON;
    }



    public static Stock ToStock(JSONObject stockJSON) throws JSONException, ParseException {
        String stockSymbol = stockJSON.getString("stockSymbol");
        String companyName = stockJSON.getString("companyName");
        Log.d("TAG", "ToStock: "+ stockSymbol + " "+  companyName);
        return new Stock(stockSymbol, companyName,0, 0, 0);
    }

    @Override
    public int compareTo(Stock o) {
        int compare = this.getStockSymbol().compareTo(o.getStockSymbol());
        if(compare > 0){
            return 1;
        } else if (compare < 0){
            return -1;
        } else{
            return 0;
        }

    }
}
