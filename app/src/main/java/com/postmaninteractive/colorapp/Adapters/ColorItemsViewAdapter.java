package com.postmaninteractive.colorapp.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.postmaninteractive.colorapp.MainActivity;
import com.postmaninteractive.colorapp.Models.ColorItem;
import com.postmaninteractive.colorapp.R;

import java.util.List;

public class ColorItemsViewAdapter extends RecyclerView.Adapter<ColorItemsViewAdapter.ColorItemViewHolder> {

    private final Context context;
    private final List<ColorItem> colorItems;

    public ColorItemsViewAdapter(Context context, List<ColorItem> colorItems) {
        this.context = context;
        this.colorItems = colorItems;
    }

    @NonNull
    @Override
    public ColorItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_color_item, viewGroup, false);
        return new ColorItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorItemViewHolder colorItemViewHolder, int i) {

        final ColorItem colorItem = colorItems.get(i);
        colorItemViewHolder.setBackgroundColor(colorItem.getColorString());
        colorItemViewHolder.setColorName(colorItem.getGeneralName());
        colorItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.setAsBackgroundColor(Color.parseColor(colorItem.getColorString()), colorItem.getColorString());
            }
        });

    }

    @Override
    public int getItemCount() {
        return colorItems.size();
    }

    static class ColorItemViewHolder extends RecyclerView.ViewHolder {

        private final CardView cvColor;
        private final TextView tvName;

        private ColorItemViewHolder(@NonNull View itemView) {
            super(itemView);

            cvColor = itemView.findViewById(R.id.cvColor);
            tvName = itemView.findViewById(R.id.tvName);
        }

        private void setBackgroundColor(String colorString) {

            cvColor.setBackgroundColor(Color.parseColor(colorString));
        }

        private void setColorName(String name) {
            tvName.setText(name);
        }
    }

}