package com.example.stockwatch;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class StockDownloader {
    private static final String TAG = "StockFinDownloader";
    private static final String DATA_URL = "https://cloud.iexapis.com/stable/stock/";
    private static final String yourAPIKeyWq = "/quote?token=pk_62ed95d3946b4c5eb297d48ef7e2a0c5";
    MainActivity mainActivity;
    public static HashMap<String, String> symbolNameMap = new HashMap<>();
    public static Stock tempStock;



    public static HashMap<String, String> getSymbolNameMap() {
        return symbolNameMap;
    }

//    public static HashMap<String, String> symbolNameMap = new HashMap<>();

    public static void getFinDataAsStock(String inputSymbol, MainActivity mainActivity) throws JSONException {

        RequestQueue queue = Volley.newRequestQueue(mainActivity);

        String urlToUse = getUrlToUse(inputSymbol);

        Response.Listener<JSONObject> listener =
                response -> {
                    try {
                        handleResults(mainActivity,response.toString());
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    //Log.e(TAG, "getFinDataAsStock: " + parseJSONandReturnStock(response.toString()));
                };

        Response.ErrorListener error = error1 -> {
            Log.d(TAG, "getFinData failed: ");
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(new String(error1.networkResponse.data));
                Log.d(TAG, "getSourceData: " + jsonObject);
                handleResults(mainActivity, null);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        };

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error);
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    @NonNull
    public static String getUrlToUse(String inputSymbol) {
        Uri.Builder buildURL = Uri.parse(DATA_URL).buildUpon();

        String urlToUse = buildURL.build().toString();

        urlToUse = urlToUse + inputSymbol + yourAPIKeyWq;
        return urlToUse;
    }


    private static void handleResults(MainActivity mainActivity, String s) throws JSONException, IOException {

        if (s == null) {
            Log.d(TAG, "handleResults: Failure in fin data download");
            mainActivity.downloadFailed();
            return;
        }
        mainActivity.updateData(parseJSONandReturnStock(s));
    }

    private static void Logmap( MainActivity mainActivity) {
        Set keys = symbolNameMap.keySet();
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String value = (String) symbolNameMap.get(key);
            Log.e(TAG, "parseJSON: " + key + " = " + value);
        }
    }

    private static Stock parseJSONandReturnStock(String s) {

        String stockSymbol = null;
        String companyName = null;
        double currentPrice = 0;
        double priceChange = 0;
        double changePercent = 0;
        try {
            // Did not write code to handle null for getting price as it's already handled by function and exception
            JSONObject jObjMain = new JSONObject(s);
            stockSymbol = jObjMain.getString("symbol");
            companyName = jObjMain.getString("companyName");
            currentPrice = jObjMain.getDouble("latestPrice");
            priceChange = jObjMain.getDouble("change");
            changePercent = jObjMain.getDouble("changePercent");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new Stock(stockSymbol, companyName, currentPrice, priceChange, changePercent);
    }


}
