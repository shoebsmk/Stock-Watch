package com.example.stockwatch;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private final String TAG = getClass().getSimpleName();
    private final MainActivity mainActivity;
    private final ArrayList<Stock> stockArrayList;

    public StockAdapter(MainActivity mainActivity, ArrayList<Stock> stockArrayList) {
        this.mainActivity = mainActivity;
        this.stockArrayList = stockArrayList;
    }


    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_entry,parent,false);
        itemView.setOnLongClickListener(mainActivity);
        itemView.setOnClickListener(mainActivity);
        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stockArrayList.get(position);
        holder.stockSymbolTV.setText(stock.getStockSymbol());
        holder.companyNameTV.setText(stock.getCompanyName());
        holder.priceTV.setText(String.valueOf(stock.getCurrentPrice()));
        holder.priceChangeTV.setText(String.valueOf(stock.getPriceChange()));
        holder.changePercentTV.setText(String.valueOf("("+ stock.getChangePercent()) + "%)");
        if(stock.getPriceChange() < -0.009){
            holder.indicatorTV.setText("▼");
            holder.indicatorTV.setTextColor(Color.rgb(251, 13, 13));
            holder.priceTV.setTextColor(Color.rgb(251, 13, 13));
            holder.priceChangeTV.setTextColor(Color.rgb(251, 13, 13));
            holder.changePercentTV.setTextColor(Color.rgb(251, 13, 13));

        } else if (stock.getPriceChange() > 0.009){
            holder.indicatorTV.setText("▲");
            holder.indicatorTV.setTextColor(Color.rgb(117, 255, 51));
            holder.priceTV.setTextColor(Color.rgb(117, 255, 51));
            holder.priceChangeTV.setTextColor(Color.rgb(117, 255, 51));
            holder.changePercentTV.setTextColor(Color.rgb(117, 255, 51));
        } else {
            holder.indicatorTV.setText("");
        }


    }

    @Override
    public int getItemCount() {
        return stockArrayList.size();
    }
}
