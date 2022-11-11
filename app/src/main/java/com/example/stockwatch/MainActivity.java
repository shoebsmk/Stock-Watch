package com.example.stockwatch;

import static com.example.stockwatch.StockDownloader.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private ArrayList<Stock> stockArrayList;
    private ArrayList<Stock> stockArrayList1 = new ArrayList<>();
    ArrayList<String> symbols = new ArrayList<>();

    private RecyclerView recyclerView;
    private StockAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout swiper;

    private boolean loadingFirstTime = true;
    private Stock clickedStock = null;
    private static String clickURL = "https://www.microsoft.com";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stockArrayList = new ArrayList<>();
        symbols = new ArrayList<>();

        recyclerView = findViewById(R.id.myRV);
        adapter = new StockAdapter(this,stockArrayList);
        recyclerView.setAdapter(adapter);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        swiper = findViewById(R.id.swiper);

        swiper.setOnRefreshListener(() -> {
            try {
                if(isConnected()){
                    if(loadingFirstTime){
                        //Load symbols when started without net (Optimization)
                        NameDownloader.getSourceData(this);
                        loadingFirstTime = false;
                    }
                    redownloadData();
                } else {
                    notConnectedDialog();
                    downloadFailed();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            swiper.setRefreshing(false); // This stops the busy-circle
        });

        try {
            LoadDataFromFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(isConnected()){
            NameDownloader.getSourceData(this);
            try {
                redownloadData();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            notConnectedDialog();
            downloadFailed();
        }

        Log.d(TAG, "onCreate: is connected  " + isConnected());

    }

    public boolean isConnected() {
        if (hasNetworkConnection()){
            return true;
        } else{
            return false;
        }
    }

    private boolean hasNetworkConnection() {
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }

    private void LoadDataFromFile() throws FileNotFoundException {
        FileInputStream fis;
        try{
            fis = getApplicationContext().openFileInput("STOCK-SYMBOL-NAME-DATA.json");
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
            return;
        }
        try{
            StringBuilder fileContent = new StringBuilder();
            byte [] buffer = new byte[4000];
            int n;
            while((n=fis.read(buffer)) != -1){
                fileContent.append(new String(buffer,0,n));
            }
            JSONArray noteListJSONARR = new JSONArray(fileContent.toString());
            Log.d(TAG, "LoadDataFromFile: " );
            for(int i = 0 ; i< noteListJSONARR.length(); i++){
                //logic to use list to download new stock data to display

                if(isConnected()){
                    StockDownloader.getFinDataAsStock(Stock.ToStock(noteListJSONARR
                            .getJSONObject(i)).getStockSymbol(),this);
                }
                else {
                    stockArrayList.add(Stock.ToStock(noteListJSONARR.getJSONObject(i)));
                    adapter.notifyItemRangeChanged(0,stockArrayList.size());
                }
            }
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }

    }

    private void SaveDataToFile() throws JSONException, IOException {
        JSONArray stockListJSONARR = new JSONArray();
        for(Stock stock:stockArrayList){
            stockListJSONARR.put(stock.toJSON());
        }
        FileOutputStream fos = getApplicationContext()
                .openFileOutput("STOCK-SYMBOL-NAME-DATA.json",MODE_PRIVATE);
        PrintWriter writer = new PrintWriter(fos);
        writer.println(stockListJSONARR);
        writer.close();
        fos.close();
        Log.d(TAG, "SaveDataToFile: SAVED");
    }


    //Long Click
    @Override
    public boolean onLongClick(View view) {
        promptDeleteDialog(view);
        return false;
    }

    private void promptDeleteDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_menu_delete);
        builder.setPositiveButton("DELETE",(dialog, id) -> {
            try {
                DeleteView(view);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        builder.setNegativeButton("CANCEL",(dialog, id) -> {});
        int pos = recyclerView.getChildLayoutPosition(view);
        Stock stock = stockArrayList.get(pos);
        builder.setTitle("Delete Stock");
        builder.setMessage("Delete stock '" + stock.getCompanyName() + "' ?" );
        AlertDialog dialog =  builder.create();
        dialog.show();

    }

    private void DeleteView(View view) throws JSONException, IOException {
        int pos = recyclerView.getChildLayoutPosition(view);
        stockArrayList.remove(pos);
        adapter.notifyItemRemoved(pos);
        symbols.clear();
        for(Stock i:stockArrayList){
            symbols.add(i.getStockSymbol());
        }
        SaveDataToFile();
    }


    @Override
    protected void onPause() {
        try {
            SaveDataToFile();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        super.onPause();
    }


    private void makeErrorAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(msg);
        builder.setTitle("No App Found");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        int pos = recyclerView.getChildLayoutPosition(view);
        if(stockArrayList.size() == 0){
            return;
        }
        clickedStock = stockArrayList.get(pos);
        clickURL =  "http://www.marketwatch.com/investing/stock/"
                + stockArrayList.get(pos).getStockSymbol();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickURL));
        // Check if there is an app that can handle https intents
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            makeErrorAlert("No Application found that handles ACTION_VIEW (https) intents");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_menu_btn){
            if(isConnected()){
                onAddBtnClicked();
            } else {
                // ADD prompt to
                notConnectedDialog();
            }
            return true;}
        return super.onOptionsItemSelected(item);
    }

    public void notConnectedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", (dialog, id) -> {});
        builder.setMessage("Stocks cannot be updated without a network connection.");
        builder.setTitle("No Network Connection");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void symbolNotFoundDialog(String notFound) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", (dialog, id) -> {});
        builder.setTitle("Symbol/Name not found: " + notFound);
        builder.setMessage("Data for Stock symbol.");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void stockExistsDialog(String existingStock) {
        // Simple dialog - ok button.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("OK", (dialog, id) -> {});
        builder.setMessage("Stock Symbol: " + existingStock + " is already displayed.");
        builder.setTitle("Duplicate Stock! ");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void onAddBtnClicked() {

        // Single input value dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create an edittext and set it to be the builder's view
        final EditText et = new EditText(this);
        et.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        et.setAllCaps(true);
        builder.setView(et);
        // lambda can be used here (as is below)
        builder.setPositiveButton("OK", (dialog, id) -> {
            HashMap<String, String> resultStocksMap = new HashMap<>();
            resultStocksMap = NameDownloader.StockSearch(et.getText().toString());
            if(resultStocksMap.size() == 0){
                symbolNotFoundDialog(et.getText().toString());
            }
            else if(resultStocksMap.size() == 1) {
                String onlySymbol = (String) resultStocksMap.keySet().toArray()[0];
                try {
                    // Check if exists
                    for(Stock stock : stockArrayList){
                        if(stock.getStockSymbol().contains(onlySymbol)){
                            stockExistsDialog(onlySymbol);
                            return;
                        }
                    }
                    getFinDataAsStock(onlySymbol,this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                //
                resultStocksMap.size();
                SelectFromResultMaps(resultStocksMap);
            }

        });

        // lambda can be used here (as is below)
        builder.setNegativeButton("CANCEL", (dialog, id) -> {

        });

        builder.setMessage("Please enter a stock symbol:");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    private void SelectFromResultMaps(HashMap<String, String> resultStocksMap) {
        Log.d("TAG", "onAddBtnClicked: " + resultStocksMap.values());
        final int size = resultStocksMap.values().size();
        Log.d(TAG, "SelectFromResultMaps: " + size);

        // List selection dialog
        //make an array of strings

        final CharSequence[] sArray = new CharSequence[size];

        for (int i = 0; i <size; i++){
            sArray[i] = resultStocksMap.values().toArray()[i].toString();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make a selection");

        // Set the builder to display the string array as a selectable
        // list, and add the "onClick" for when a selection is made
        builder.setItems(sArray, (dialog, which) -> {
            String keySelected = null;
            for(String i : resultStocksMap.keySet()){
                if(resultStocksMap.get(i).contains(sArray[which])){
                    Log.e(TAG, "key: " + i);
                    keySelected = i;

                }
            }
            try {
                for(Stock stock : stockArrayList){
                    if(stock.getStockSymbol().contains(keySelected)){
                        stockExistsDialog(keySelected);
                        return;
                    }
                }
                    getFinDataAsStock(keySelected,this);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        });

        builder.setNegativeButton("Nevermind", (dialog, id) -> {
        });
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    public void downloadFailed() {
        if(stockArrayList.size() == 0) {
            return;
        }
        for(Stock s : stockArrayList){
            s.setChangePercent(0.00);
            s.setCurrentPrice(0.00);
            s.setPriceChange(0.00);
        }
        Collections.sort(stockArrayList);
        adapter.notifyDataSetChanged();
    }

    public void updateData(Stock stock) throws JSONException, IOException {

        stockArrayList.add(stock);
        adapter.notifyItemRangeChanged(0, stockArrayList.size());
        symbols.clear();
        for(Stock i:stockArrayList){
            symbols.add(i.getStockSymbol());
        }
        SaveDataToFile();
        Collections.sort(stockArrayList);
        adapter.notifyItemRangeChanged(0,stockArrayList.size());
    }
    public void redownloadData() throws JSONException {

        //used to dlnd symbol here
        symbols.clear();
        for(Stock i:stockArrayList){
            symbols.add(i.getStockSymbol());
        }
        stockArrayList.clear();
        for(String i:symbols){
            if (i ==null){
                return;
            }
            getFinDataAsStock(i,this);
        adapter.notifyDataSetChanged();
        //adapter.notifyItemRangeChanged(0, stockArrayList.size());
        }

    }
}