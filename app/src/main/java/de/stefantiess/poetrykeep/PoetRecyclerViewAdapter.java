package de.stefantiess.poetrykeep;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PoetRecyclerViewAdapter {
    private static final String TAG = "PoetRecyclerViewAdapter";


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mpoetIconView;
        TextView mPoetNameView;
        LinearLayout mParentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            mParentLayout = itemView.findViewById(R.id.poet_list_item);
            mpoetIconView = itemView.findViewById(R.id.poet_icon_view);
            mPoetNameView = itemView.findViewById(R.id.poet_name_view);
            //TODO: Complete Recycler View Adapter


        }
    }

}
