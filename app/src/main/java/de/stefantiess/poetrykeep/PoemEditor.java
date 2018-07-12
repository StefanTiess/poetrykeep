package de.stefantiess.poetrykeep;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import de.stefantiess.poetrykeep.database.PoemContract;
import de.stefantiess.poetrykeep.database.PoemContract.PoemEntry;
import de.stefantiess.poetrykeep.database.PoemsDatabaseHelper;

public class PoemEditor extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    EditText editAuthor;
    EditText editTitle;
    EditText editPoemText;
    EditText editYear;
    Spinner editLanguague;
    Button saveButton;
    Button chancelButton;
    PoemsDatabaseHelper mPoemHelper;
    Uri currentUri = null;
    Boolean isNewPoem = true;
    int mID = 0;
    private static final int POEM_LOADER = 3;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poem_editor);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //Bind Views
        editAuthor = findViewById(R.id.editAuthor);
        editTitle = findViewById(R.id.editTitle);
        editPoemText = findViewById(R.id.editOriginalText);
        editYear = findViewById(R.id.editYear);
        editLanguague = findViewById(R.id.languagueSpinner);
        saveButton = findViewById(R.id.saveButton);
        chancelButton = findViewById(R.id.chancelButton);
        mPoemHelper = new PoemsDatabaseHelper(this);

        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            try {
                mID = intentExtras.getInt("id", 0);
            } catch (NullPointerException e) {
                Log.e("Poem Editor", "Cold not find id in Intent Extras: " + e.toString());
            }
        }
        if (mID > 0) {
            isNewPoem = false;
            this.setTitle("Edit");
            currentUri = ContentUris.withAppendedId(PoemEntry.CONTENT_URI,mID);
            getLoaderManager().initLoader(POEM_LOADER, null, this);


        }


        Resources res = getResources();
        SpinnerAdapter languageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, res.getStringArray(R.array.languages));
        editLanguague.setAdapter(languageAdapter);

        //set Clicklisteners
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: save Edits to Database
                saveEntries();
            }
        });

        chancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: confirmation popup if one of the textfields is not empty

                // PopupWindow popupWindow = new PopupWindow();

                //Return without saving
                finish();
            }
        });


    }

    public PoemEditor() {
    }

    private void saveEntries() {
        if (editAuthor.getText().toString().length() > 0 && editTitle.getText().toString().length() > 0 && editPoemText.getText().toString().length() > 0) {
            PoemsDatabaseHelper dbHelper = new PoemsDatabaseHelper(getApplicationContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            ContentValues poemValues = new ContentValues();
            poemValues.put(PoemEntry.COLUMN_AUTHOR_NAME,editAuthor.getText().toString());
            poemValues.put(PoemEntry.COLUMN_ORIGINAL_TITLE_NAME,editTitle.getText().toString());
            poemValues.put(PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME,editPoemText.getText().toString());
            if (editYear.getText().toString().length() > 0) {
                poemValues.put(PoemEntry.COLUMN_PUBLICATION_YEAR_NAME,editYear.getText().toString());

            }
            String language = editLanguague.getSelectedItem().toString();
            poemValues.put(PoemEntry.COLUMN_ORIGINAL_LANGUAGE_NAME, language);
            if (isNewPoem) {
                Uri newUri = getContentResolver().insert(PoemEntry.CONTENT_URI, poemValues);
                if (newUri == null) {
                    Toast.makeText(this,"Error creating new Poem", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this,"Poem saved", Toast.LENGTH_SHORT).show();

                }
            }
            else {
                int affectedRows = getContentResolver().update(currentUri, poemValues, null,null);
                if (affectedRows == 0) {
                    Toast.makeText(this,"Error updating Poem", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this,"Poem saved", Toast.LENGTH_SHORT).show();

                }
            }
            finish();


        }
        else {
            if (editAuthor.getText().toString().length() == 0) {
                editAuthor.setHintTextColor(Color.RED);

            }
            if (editTitle.getText().toString().length() == 0) {
                editTitle.setHintTextColor(Color.RED);

            }
            if (editPoemText.getText().toString().length() == 0) {
                editPoemText.setHintTextColor(Color.RED);
            }
         }
    }

    private void deleteEntries() {
        if (isNewPoem) {
            finish();
        }
        else if (currentUri != null){
            int affectedRows = getContentResolver().delete(currentUri, null, null);
            if (affectedRows == 0) {
                Toast.makeText(this,"Error updating Poem", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this,"Poem deleted", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        }
    }

    private void populateEditTextField (Poem poem) {
        editAuthor.setText(poem.getAuthor());
        editPoemText.setText(poem.getPoemBody());
        editTitle.setText(poem.getTitle());
        editYear.setText(String.valueOf(poem.getYear()));
        editLanguague.setSelection(poem.getLanguageID());
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
            populateEditTextField(mPoemHelper.makePoemFromFirstCursor(data));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            saveEntries();
            return true;
        }

        if (id == R.id.action_delete) {
            deleteEntries();
        }

        return super.onOptionsItemSelected(item);
    }
}
