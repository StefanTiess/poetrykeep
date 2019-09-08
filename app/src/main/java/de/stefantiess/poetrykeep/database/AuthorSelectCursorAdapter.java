package de.stefantiess.poetrykeep.database;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import de.stefantiess.poetrykeep.R;


public class AuthorSelectCursorAdapter extends CursorAdapter {
        public AuthorSelectCursorAdapter(Context context, Cursor c) {
            super(context, c, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {


            return LayoutInflater.from(context).inflate(R.layout.poet_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {



            TextView author = view.findViewById(R.id.poet_name_view);
            author.setText(cursor.getString(cursor.getColumnIndexOrThrow(PoemContract.PoemEntry.COLUMN_AUTHOR_NAME)));



        }




}
