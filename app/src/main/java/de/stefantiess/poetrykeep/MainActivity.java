package de.stefantiess.poetrykeep;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import de.stefantiess.poetrykeep.database.PoemCardCursorAdapter;
import de.stefantiess.poetrykeep.database.PoemContract;
import de.stefantiess.poetrykeep.database.PoemContract.PoemEntry;
import de.stefantiess.poetrykeep.database.PoemsDatabaseHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    PoemsDatabaseHelper poemsHelper;
    private static final int POEM_LOADER = 1;
    PoemCardCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton addPoemButton = (FloatingActionButton) findViewById(R.id.add_poem);
        addPoemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), PoemEditor.class );
                startActivity(i);
            }
        });
        poemsHelper = new PoemsDatabaseHelper(this);

       populateLatestPoemsList();
       getLoaderManager().initLoader(POEM_LOADER, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.add_dummy_data) {
            insertDummyData();
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.

        // Create and/or open a database to read from it
        SQLiteDatabase db = poemsHelper.getReadableDatabase();

        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.
        Cursor cursor = db.rawQuery("SELECT * FROM " + PoemEntry.TABLE_NAME, null);
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
           Log.v("DB Info", "Number of rows in pets database table: " + cursor.getCount());
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    private void insertDummyData() {

        //Add two dummy poems
        ContentValues reisen = new ContentValues();
        reisen.put(PoemEntry.COLUMN_AUTHOR_NAME, "Gottfried Benn");
        reisen.put(PoemEntry.COLUMN_ORIGINAL_TITLE_NAME, "Reisen");
        reisen.put(PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME,
                "       Meinen Sie Zürich zum Beispiel"
                        + "\n sei eine tiefere Stadt,"
                        + "\n wo man Wunder und Weihen"
                        + "\n immer als Inhalt hat?"
                        + "\n "
                        + "\n         Meinen Sie, aus Habana,"
                        + "\n weiß und hibiskusrot,"
                        + "\n bräche ein ewiges Manna"
                        + "\n für Ihre Wüstennot?"
                        + "\n "
                        + "\n         Bahnhofstraßen und Rueen,"
                        + "\n Boulevards, Lidos, Laan –"
                        + "\n selbst auf den Fifth Avenueen"
                        + "\n fällt Sie die Leere an –"
                        + "\n "
                        + "\n ach, vergeblich das Fahren!"
                        + "\n         Spät erst erfahren Sie sich:"
                        + "\n bleiben und stille bewahren"
                        + "\n das sich umgrenzende Ich.");
        reisen.put(PoemEntry.COLUMN_PUBLICATION_YEAR_NAME, 1950);
        reisen.put(PoemEntry.COLUMN_ORIGINAL_LANGUAGE_NAME, PoemEntry.LANGUAGE_GERMAN);

        Uri newUri = getContentResolver().insert(PoemEntry.CONTENT_URI, reisen);

        displayDatabaseInfo();
    }

    private void populateLatestPoemsList() {
        mCursorAdapter = new PoemCardCursorAdapter(this, null);
        final ListView container = findViewById(R.id.poems_container);
        container.setEmptyView(findViewById(R.id.empty_view));
        container.setDivider(null);
        container.setAdapter(mCursorAdapter);
        container.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) mCursorAdapter.getItem(position);
                Poem p = poemsHelper.makePoemFromCursor(c);
                int poemID = p.getID();
                Intent i = new Intent(getApplicationContext(), PoemView.class);
                i.putExtra("id", poemID);
                i.putExtra("author",p.getAuthor());
                i.putExtra("title", p.getTitle() );
                i.putExtra("text", p.getPoemBody());
                i.putExtra("year", p.getYear());

                startActivity(i);

            }
        });



    }


/*
    private ArrayList<Poem> getPoemsFromDatabase(@Nullable String orderby) {
        ArrayList<Poem> poems = new ArrayList<>();

        String[] projection = {PoemEntry._ID,
                PoemEntry.COLUMN_AUTHOR_NAME,
                PoemEntry.COLUMN_ORIGINAL_TITLE_NAME,
                PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME,
                PoemEntry.COLUMN_PUBLICATION_YEAR_NAME,
                PoemEntry.COLUMN_ORIGINAL_LANGUAGE_NAME};


        Cursor c = getContentResolver().query(PoemEntry.CONTENT_URI, projection,null,null,null);
        try {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndex(PoemEntry._ID));
                String author = c.getString(c.getColumnIndex(PoemEntry.COLUMN_AUTHOR_NAME));
                String title = c.getString(c.getColumnIndex(PoemEntry.COLUMN_ORIGINAL_TITLE_NAME));
                String text = c.getString(c.getColumnIndex(PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME));
                int year = 0;
                if (c.getColumnIndex(PoemEntry.COLUMN_PUBLICATION_YEAR_NAME) != -1) {
                    year = c.getInt(c.getColumnIndex(PoemEntry.COLUMN_PUBLICATION_YEAR_NAME));
                }

                int language = c.getInt(c.getColumnIndexOrThrow(PoemEntry.COLUMN_ORIGINAL_LANGUAGE_NAME));
                poems.add(new Poem(id, author, title, text, year, language));
            }
        } finally {
        }

        return poems;
    }

    private Cursor getPoemsCursorFromDatabase(@Nullable String orderby) {

        String[] projection = {PoemEntry._ID,
                PoemEntry.COLUMN_AUTHOR_NAME,
                PoemEntry.COLUMN_ORIGINAL_TITLE_NAME,
                PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME,
                PoemEntry.COLUMN_PUBLICATION_YEAR_NAME,
                PoemEntry.COLUMN_ORIGINAL_LANGUAGE_NAME};


       Cursor c = getContentResolver().query(PoemEntry.CONTENT_URI, projection,null,null,null);
        return c;
    }
*/

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {PoemEntry._ID,
                PoemEntry.COLUMN_AUTHOR_NAME,
                PoemEntry.COLUMN_ORIGINAL_TITLE_NAME,
                PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME,
                PoemEntry.COLUMN_PUBLICATION_YEAR_NAME,
                PoemEntry.COLUMN_ORIGINAL_LANGUAGE_NAME};

        return new CursorLoader(this, PoemEntry.CONTENT_URI, projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
       mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
}
