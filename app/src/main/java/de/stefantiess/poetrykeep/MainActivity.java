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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import de.stefantiess.poetrykeep.database.PoemCardCursorAdapter;
import de.stefantiess.poetrykeep.database.PoemContract;
import de.stefantiess.poetrykeep.database.PoemContract.PoemEntry;
import de.stefantiess.poetrykeep.database.PoemProvider;
import de.stefantiess.poetrykeep.database.PoemsDatabaseHelper;
import de.stefantiess.poetrykeep.database.WordpressHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    PoemsDatabaseHelper poemsHelper;
    private static final int POEM_LOADER = 1;
    PoemCardCursorAdapter mCursorAdapter;
    PoetRecyclerViewAdapter mPoetAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton addPoemButton = findViewById(R.id.add_poem);
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

        if (id == R.id.action_syncWordpress) {
            syncDatabase();
        }

        return super.onOptionsItemSelected(item);
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
        if (newUri != null) {
            Toast.makeText(this, "Test Poem Created", Toast.LENGTH_LONG).show();
        }
    }

    private void populateLatestPoemsList() {
        mCursorAdapter = new PoemCardCursorAdapter(this, null);
        final ListView PoemListContainer = findViewById(R.id.poems_container);
        PoemListContainer.setEmptyView(findViewById(R.id.empty_view));
        PoemListContainer.setDivider(null);
        PoemListContainer.setAdapter(mCursorAdapter);
        PoemListContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) mCursorAdapter.getItem(position);
                int poemID = c.getInt(c.getColumnIndexOrThrow(PoemEntry._ID));
                Intent i = new Intent(getApplicationContext(), PoemView.class);
                i.putExtra("id", poemID);
                startActivity(i);
            }
        });
        //todo: Complete list Touch behaviour and adapter invocation;
        /*
        final RecyclerView PoetContainerView = findViewById(R.id.poet_container);

        mPoetAdapter = new PoetRecyclerViewAdapter();
        PoetContainerView.setAdapter(mPoetAdapter);
        PoetContainerView.OnItemTouchListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) .getItem(position);
                int poemID = c.getInt(c.getColumnIndexOrThrow(PoemEntry._ID));
                Intent i = new Intent(getApplicationContext(), PoemView.class);
                i.putExtra("id", poemID);
                startActivity(i);
            }
        });*/




    }


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


    private void syncDatabase() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        try {
            activeNetwork = cm.getActiveNetworkInfo();
        } catch (NullPointerException e) {
            Log.e("Active Network Info", "" + e);
        }
        if (activeNetwork.isConnectedOrConnecting()) {

            BackgroundSync sync = new BackgroundSync();
            PoemProvider p = new PoemProvider();
            sync.execute(getContentResolver());
        } else {
            Toast.makeText(this, "No Internet, Fool!", Toast.LENGTH_LONG).show();
        }
    }

    private class BackgroundSync extends AsyncTask<ContentResolver, Void, Boolean> {


        @Override
        protected Boolean doInBackground(ContentResolver... provider) {

            WordpressHelper helper = new WordpressHelper(provider[0]);
            return helper.syncDatabase();

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Toast.makeText(getApplicationContext(), "Sync Sucessfull", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Sync Failed, sorrey!", Toast.LENGTH_SHORT).show();

            }
        }
    }


}
