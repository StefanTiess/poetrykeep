package de.stefantiess.poetrykeep.database;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.stefantiess.poetrykeep.R;


public class PoetRecyclerViewAdapter extends BaseCursorAdapter<PoetRecyclerViewAdapter.AuthorHolder> {
    private static final String TAG = "PoetRecyclerViewAdapter";
    OnAuthorClickListener mAuthorClicklistener;

    public PoetRecyclerViewAdapter(OnAuthorClickListener authorClickListener) {
        super(null);
        this.mAuthorClicklistener = authorClickListener;
    }

    @Override
    public AuthorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View formNameView = LayoutInflater.from(parent.getContext()).inflate(R.layout.poet_list_item, parent, false);
        return new AuthorHolder (parent.getContext(), formNameView,mAuthorClicklistener);
    }

    @Override
    public void onBindViewHolder(AuthorHolder holder, Cursor cursor) {
        int mColumnIndexName = cursor.getColumnIndex(PoemContract.PoemEntry._ID);
        String firstLetter = "";
        String lastLetter = "";

        String authorName = cursor.getString(cursor.getColumnIndexOrThrow(PoemContract.PoemEntry.COLUMN_AUTHOR_NAME));
        authorName = authorName.trim();
        holder.mPoetNameView.setText(authorName);

        // Split Name into Initial Letters
        String[] nameParts = authorName.split(" ");
        if (nameParts.length > 0) {
            firstLetter = String.valueOf(nameParts[0].charAt(0));
            if (nameParts.length > 1 ) {
                lastLetter = String.valueOf(nameParts[nameParts.length-1].charAt(0));
            }
        // Set text of Initials-Circle
        holder.mPoetInitials.setText(firstLetter + lastLetter);
        }
    }

    @Override
    public void swapCursor(Cursor newCursor) {
        super.swapCursor(newCursor);
    }

    public class AuthorHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mPoetNameView;
        TextView mPoetInitials;
        LinearLayout mParentLayout;

        String author;
        Context context;
        OnAuthorClickListener authorClickListener;

        public AuthorHolder(Context context, View itemView, OnAuthorClickListener authorClickListener) {
            super(itemView);
            this.context = context;
            this.authorClickListener = authorClickListener;

            // 2. Set up the UI widgets of the holder
            mParentLayout = itemView.findViewById(R.id.poet_list_item);
            mPoetNameView = itemView.findViewById(R.id.poet_name_view);
            mPoetInitials = itemView.findViewById(R.id.poet_name_circle);


            // 3. Set the "onClick" listener of the holder
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
              authorClickListener.onAuthorClick(getAdapterPosition());
        }
    }
    public interface OnAuthorClickListener {
        void onAuthorClick(int position);
    }
}
