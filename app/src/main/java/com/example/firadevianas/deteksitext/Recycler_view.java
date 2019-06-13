package com.example.firadevianas.deteksitext;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Firadevianas on 2/11/2019.
 */

public class Recycler_view extends RecyclerView.ViewHolder{
    TextView text_date,text_amount,text_desc,text_category,text_title,text_typetrans;
    public Recycler_view(View itemView) {
        super(itemView);

        text_date = itemView.findViewById(R.id.txt_date);
        text_amount = itemView.findViewById(R.id.txt_amount);
        text_category = itemView.findViewById(R.id.txt_category);
        text_title = itemView.findViewById(R.id.txt_title);

    }
}
