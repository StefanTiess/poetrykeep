package de.stefantiess.poetrykeep.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.stefantiess.poetrykeep.Poem;
import de.stefantiess.poetrykeep.database.PoemContract.PoemEntry;

public class PoemsDatabaseHelper extends SQLiteOpenHelper {


    private static final int DB_VERSION  = 1;
    private static final String DB_NAME = "poems.db";

    public PoemsDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ENTRIES = "CREATE TABLE " + PoemEntry.TABLE_NAME + " ("
                                + PoemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + PoemEntry.COLUMN_AUTHOR_NAME+ " TEXT NOT NULL, "
                                + PoemEntry.COLUMN_ORIGINAL_TITLE_NAME + " TEXT NOT NULL, "
                                + PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME + " TEXT NOT NULL, "
                                + PoemEntry.COLUMN_PUBLICATION_YEAR_NAME + " INTEGER, "
                                + PoemEntry.COLUMN_ORIGINAL_LANGUAGE_NAME + " INTEGER NOT NULL DEFAULT 0);";

        db.execSQL(SQL_CREATE_ENTRIES);






    }

    public Poem makePoemFromCursor (Cursor c) {
        Poem p = null;
        c.moveToFirst();
        try {

            int id = c.getInt(c.getColumnIndex(PoemEntry._ID));
            String author = c.getString(c.getColumnIndex(PoemEntry.COLUMN_AUTHOR_NAME));
            String title = c.getString(c.getColumnIndex(PoemEntry.COLUMN_ORIGINAL_TITLE_NAME));
            String text = c.getString(c.getColumnIndex(PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME));
            int year = 0;
            if (c.getColumnIndex(PoemEntry.COLUMN_PUBLICATION_YEAR_NAME) != -1) {
                year = c.getInt(c.getColumnIndex(PoemEntry.COLUMN_PUBLICATION_YEAR_NAME));
            }

            int language = c.getInt(c.getColumnIndexOrThrow(PoemEntry.COLUMN_ORIGINAL_LANGUAGE_NAME));
            p = new Poem(id, author, title, text, year, language);

        } finally {
            // c.close();
        }

        return p;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



}
