package de.stefantiess.poetrykeep;

import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.stefantiess.poetrykeep.database.PoemContract;
import de.stefantiess.poetrykeep.database.PoemsDatabaseHelper;

public class PoemView extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static String LOG_TAG = "Poem View";
    int mID = 0;
    TextView mTitleView;
    TextView mAuthorView;
    TextView mTextBodyView;
    TextView mYearView;
    LinearLayout mPoemTextContainer;
    MenuItem mLearnModeButton;

    String fullPoem;
    String partialPoem = "";
    String[] poemLines = null;
    int lineIndex = 0;

    PoemsDatabaseHelper mPoemHelper;
    private boolean learnMode = false;
    private static final int POEM_LOADER = 2;

    View.OnClickListener tapListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (lineIndex < poemLines.length) {
                partialPoem += poemLines[lineIndex] + "\n";
                mTextBodyView.setText(partialPoem);
                lineIndex++;
            } else {
                endLearnMode();

            }
        }

    };



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poem_view);
        mAuthorView = findViewById(R.id.poemview_author_text_view);
        mTitleView = findViewById(R.id.poemview_title_text_view);
        mTextBodyView = findViewById(R.id.poemview_textbody_textview);
        mYearView = findViewById(R.id.poemview_year_textview);
        mPoemTextContainer = findViewById(R.id.poemview_poem_container);
        mPoemHelper = new PoemsDatabaseHelper(this);

        Intent intent = getIntent();
        try {
            mID =intent.getIntExtra("id", 0);

        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Error loading Poem. Id can't be found: " + e);
        }
        if (mID > 0) {
            getLoaderManager().initLoader(POEM_LOADER, null, this);

        }

    }


    private void showPoem(Poem poem) {
        mAuthorView.setText(poem.getAuthor());
        mTitleView.setText(poem.getTitle());
        setTitle(poem.getTitle());
        mTextBodyView.setText(poem.getPoemBody());
        String year = "(" + String.valueOf(poem.getYear()) + ")";
        if (year.length() > 3) {mYearView.setText(year);}

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
        mLearnModeButton = menu.findItem(R.id.action_learnMode);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            Intent editIntent = new Intent(getApplicationContext(), PoemEditor.class);
            editIntent.putExtra("id", mID );
            startActivity(editIntent);
            return true;
        }

        if (id == R.id.action_learnMode) {
            int mode = 0;
            if (learnMode) {
                mode = 1;
            }
            switch (mode) {
                case 0: {
                    Toast.makeText(this, getText(R.string.startLearnModeMessage), Toast.LENGTH_SHORT).show();
                    startLearnMode();
                    break;
                }
                case 1: {
                    Toast.makeText(this, getText(R.string.endLearnModeMessage), Toast.LENGTH_SHORT).show();
                    endLearnMode();

                    break;
                }

                default:
                    endLearnMode();
            }

            return true;
        }



        return super.onOptionsItemSelected(item);
    }

    private void endLearnMode() {
        lineIndex = 0;
        partialPoem = "";
        Drawable brainoutline = getDrawable(R.drawable.ic_brain);
        mLearnModeButton.setIcon(brainoutline);
        mPoemTextContainer.setOnClickListener(null);
        mTextBodyView.setText(fullPoem);
        learnMode = false;


    }

    private void startLearnMode() {
        learnMode = true;
        Drawable fillebrainicon = getDrawable(R.drawable.ic_brain_filled);
        mLearnModeButton.setIcon(fillebrainicon);

        fullPoem = mTextBodyView.getText().toString();
        mTextBodyView.setText("");
        poemLines = fullPoem.split("\n");
        mPoemTextContainer.setOnClickListener(tapListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {PoemContract.PoemEntry._ID,
                PoemContract.PoemEntry.COLUMN_AUTHOR_NAME,
                PoemContract.PoemEntry.COLUMN_ORIGINAL_TITLE_NAME,
                PoemContract.PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME,
                PoemContract.PoemEntry.COLUMN_PUBLICATION_YEAR_NAME,
                PoemContract.PoemEntry.COLUMN_ORIGINAL_LANGUAGE_NAME};



        return new CursorLoader(this, ContentUris.withAppendedId(PoemContract.PoemEntry.CONTENT_URI,mID), projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            showPoem(mPoemHelper.makePoemFromFirstCursor(data));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
