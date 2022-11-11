package com.example.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class StockViewHolder extends RecyclerView.ViewHolder {
    public TextView stockSymbolTV;
    public TextView companyNameTV;
    public TextView priceTV;
    public TextView priceChangeTV;
    public TextView changePercentTV;
    public TextView indicatorTV;

    public StockViewHolder(@NonNull View itemView) {
        super(itemView);
        stockSymbolTV = itemView.findViewById(R.id.stockSymbolTV);
        companyNameTV = itemView.findViewById(R.id.companyNameTV);
        priceTV = itemView.findViewById(R.id.priceTV);
        priceChangeTV = itemView.findViewById(R.id.priceChangeTV);
        changePercentTV = itemView.findViewById(R.id.changePercentTV);
        indicatorTV = itemView.findViewById(R.id.idncicatorTV);

    }
}
