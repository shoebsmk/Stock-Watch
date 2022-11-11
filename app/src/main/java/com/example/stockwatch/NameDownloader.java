package com.example.stockwatch;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class NameDownloader {
    private static final String TAG = "StockLoaderRunnable";
    private static final String DATA_URL = "https://cloud.iexapis.com/stable/ref-data/symbols?token=pk_62ed95d3946b4c5eb297d48ef7e2a0c5";
    private static final String yourAPIKey = "pk_62ed95d3946b4c5eb297d48ef7e2a0c5";
    MainActivity mainActivity;
    public static HashMap<String, String> symbolNameMap = new HashMap<>();

    public static String getI(String j) {
        j += " SMK";
        return j;
    }

    public static int i = 786;

    //Search
    public static HashMap<String, String> StockSearch(String nameOrSymbol){

        HashMap<String, String> resultMap = new HashMap<>();
        if(symbolNameMap.size() == 0){
            Log.d(TAG, "StockSearch: " + "symbolNameMap is empty");

        }

        //search in keys
        if(symbolNameMap.containsKey(nameOrSymbol)){
            resultMap.put(nameOrSymbol,symbolNameMap.get(nameOrSymbol));
        }
        //Search in values, making temp for upper case comparison
        HashMap<String, String> symbolNameMapTemp = symbolNameMap;
        for (String i : symbolNameMapTemp.keySet()) {
            if(symbolNameMapTemp.get(i).toUpperCase().contains(nameOrSymbol)) {
                resultMap.put(i,symbolNameMap.get(i));
            }
        }


        return resultMap;
    }


    public static HashMap<String, String> getSymbolNameMap() {
        return symbolNameMap;
    }


    public static void getSourceData(MainActivity mainActivity) {

        RequestQueue queue = Volley.newRequestQueue(mainActivity);

        Uri.Builder buildURL = Uri.parse(DATA_URL).buildUpon();

        String urlToUse = buildURL.build().toString();

        Response.Listener<JSONArray> listener =
                response -> handleResults(mainActivity, response.toString());

        Response.ErrorListener error = error1 -> {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(new String(error1.networkResponse.data));
                Log.d(TAG, "getSourceData: " + jsonObject);
                handleResults(mainActivity, null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

        // Request a string response from the provided URL.
        JsonArrayRequest jsonArrayRequest =
                new JsonArrayRequest(Request.Method.GET, urlToUse,
                        null, listener, error);
        // Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);

    }



    private static void handleResults(MainActivity mainActivity, String s) {

        if (s == null) {
            Log.e(TAG, "handleResults: Failure in data download");
            mainActivity.downloadFailed();
            return;
        }
        symbolNameMap = parseJSON(s);

    }

    private static void Logmap( MainActivity mainActivity) {
        Set keys = symbolNameMap.keySet();
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String value = (String) symbolNameMap.get(key);
            Log.e(TAG, "parseJSON: " + key + " = " + value);
        }
    }

    private static HashMap<String,String> parseJSON(String s) {

        HashMap<String, String> tempHM = new HashMap<>();
        String symbol;
        String name;
        int count = 0;
        try {
            JSONArray jObjMain = new JSONArray(s);
            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject stk = (JSONObject) jObjMain.get(i);
                symbol = stk.getString("symbol");
                name = stk.getString("name");
                tempHM.put(symbol, name);
                count++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tempHM;
    }
}
