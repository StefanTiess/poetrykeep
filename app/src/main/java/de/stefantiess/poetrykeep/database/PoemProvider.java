package de.stefantiess.poetrykeep.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Objects;

import de.stefantiess.poetrykeep.database.PoemsDatabaseHelper;

import de.stefantiess.poetrykeep.database.PoemContract.PoemEntry;

import static de.stefantiess.poetrykeep.database.PoemContract.PATH_POEMS;

public class PoemProvider extends ContentProvider {
    PoemsDatabaseHelper mDbHelper;
    public static final String LOG_TAG = PoemProvider.class.getSimpleName();
    private static final int POEMS = 100;
    private static final int POEM_ID = 101;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(PoemContract.CONTENT_AUTHORITY, PATH_POEMS, POEMS);
        uriMatcher.addURI(PoemContract.CONTENT_AUTHORITY, PATH_POEMS + "/#", POEM_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new PoemsDatabaseHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = uriMatcher.match(uri);
        switch (match) {
            case POEMS:
                cursor = db.query(PoemEntry.TABLE_NAME,projection,selection,selectionArgs,null,null, sortOrder);
                break;
            case POEM_ID:
                 selection = PoemEntry._ID + "=?";
                 selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                 cursor = db.query(PoemEntry.TABLE_NAME, projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknow uri " + uri);
        }
        //Set up change notification Watchdog
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case POEMS:
                return PoemEntry.CONTENT_LIST_TYPE;
            case POEM_ID:
                return PoemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case POEMS:

                return insertPoem(uri, values);
            default: throw new IllegalArgumentException("Cannot query unknow uri " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int result;
        switch (match) {
            case POEMS:
                result= db.delete(PoemEntry.TABLE_NAME,selection,selectionArgs);
                if (result != 0) {
                    Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
                }
                return result;

            case POEM_ID:
                selection = PoemEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                result = db.delete(PoemEntry.TABLE_NAME,selection,selectionArgs);
                if (result != 0) {
                    Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
                }
                return result;


                    default: throw new IllegalArgumentException("Cannot delete unknown uri:" + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        int match = uriMatcher.match(uri);
        int result;
        switch (match) {
            case  POEMS:
                result = updatePoem(values,selection, selectionArgs);
                Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
                if (result != 0) {getContext().getContentResolver().notifyChange(uri, null);}
                return result;
            case POEM_ID:
                selection = PoemEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                result = updatePoem(values,selection, selectionArgs);
                if (result != 0) {
                    Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
                }
                return result;
            default:   throw new IllegalArgumentException("Cannot delete unknown uri:" + uri);
        }

        }

    private int updatePoem(ContentValues values, String selection, String[] selectionArgs)   {

        //Sanity Checks for Data
        if (values.containsKey(PoemEntry.COLUMN_AUTHOR_NAME)) {
            String name = values.getAsString(PoemEntry.COLUMN_AUTHOR_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Author Name Required");
            }
        }

        if (values.containsKey(PoemEntry.COLUMN_ORIGINAL_TITLE_NAME)) {
            String name = values.getAsString(PoemEntry.COLUMN_ORIGINAL_TITLE_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Poem Title Required");
            }
        }

        if (values.containsKey(PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME)) {
            String name = values.getAsString(PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Text Body Required");
            }
        }

        if (values.size() == 0) {
            return 0;
        }




        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.update(PoemEntry.TABLE_NAME,values,selection,selectionArgs);

    }

    private Uri insertPoem (Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Long id = db.insert(PoemEntry.TABLE_NAME,null, values);


        // If id is -1 that means something went wrong.
        if (id != -1) {
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri,id);

        }
        else {
            Log.e(LOG_TAG, "Error while inserting Poem in database:" + values.toString());
            return null;
        }
    }
}
