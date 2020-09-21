package de.stefantiess.poetrykeep.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class PoemContract {
    public static final String CONTENT_AUTHORITY = "de.stefantiess.poetrykeep";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_POEMS = "poems";
    public static final String PATH_AUTHORS = "authors";


    public PoemContract() {
    }

    public static final class PoemEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_POEMS);
        public static final Uri AUTHORS_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_AUTHORS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POEMS;
        public static final String AUTHOR_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_AUTHORS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POEMS;
        public static final String AUTHOR_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_AUTHORS;

        public static final String TABLE_NAME = "poems";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_AUTHOR_NAME = "author";
        public static final String COLUMN_ORIGINAL_TITLE_NAME = "org_title";
        public static final String COLUMN_ORIGINAL_TEXTBODY_NAME = "org_body";
        public static final String COLUMN_ORIGINAL_LANGUAGE_NAME = "org_language";
        public static final String COLUMN_PUBLICATION_YEAR_NAME = "year";

        //Possible Values for Languages (both original and written
        public static final int LANGUAGE_GERMAN = 0;
        public static final int LANGUAGE_ENGLISH = 1;
        public static final int LANGUAGE_FRENCH = 2;
        public static final int LANGUAGE_ITALIAN = 3;
        public static final int LANGUAGE_SPANISH = 4;
        public static final int LANGUAGE_RUSSIAN = 5;






    }
}
