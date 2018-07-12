package de.stefantiess.poetrykeep.database;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import de.stefantiess.poetrykeep.Poem;
import de.stefantiess.poetrykeep.database.PoemContract.PoemEntry;
import de.stefantiess.poetrykeep.R;

public class PoemCardCursorAdapter extends CursorAdapter {
    public PoemCardCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {


        return LayoutInflater.from(context).inflate(R.layout.poem_preview_card, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {


        TextView body = view.findViewById(R.id.poemcard_text_preview_view);
        body.setText(cursor.getString(cursor.getColumnIndexOrThrow(PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME)));

        TextView author = view.findViewById(R.id.poemcard_author_view);
        author.setText(cursor.getString(cursor.getColumnIndexOrThrow(PoemEntry.COLUMN_AUTHOR_NAME)));

        TextView title = view.findViewById(R.id.poemcard_title_view);
        title.setText(cursor.getString(cursor.getColumnIndexOrThrow(PoemEntry.COLUMN_ORIGINAL_TITLE_NAME)));

        TextView year = view.findViewById(R.id.poemcard_year_view);
        if (cursor.getInt(cursor.getColumnIndexOrThrow(PoemEntry.COLUMN_PUBLICATION_YEAR_NAME)) > 0) {
            year.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(PoemEntry.COLUMN_PUBLICATION_YEAR_NAME))));
        }
        else year.setText("");

    }
}
