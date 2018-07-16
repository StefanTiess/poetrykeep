package de.stefantiess.poetrykeep.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.stefantiess.poetrykeep.Poem;
import de.stefantiess.poetrykeep.database.PoemContract.PoemEntry;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class WordpressHelper {

    private static final String BASE_URL = "http://poetrykeep.stefantiess.de/wp-json/wp/v2/";
    private static final String PARAMETER_GET_ALL_POSTS = "posts/";
    private static final String TAG = WordpressHelper.class.getSimpleName();
    private ContentResolver mresolver;


    public WordpressHelper(ContentResolver provider) {
        mresolver = provider;
    }

    public boolean syncDatabase() {
        String url = BASE_URL + PARAMETER_GET_ALL_POSTS;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();
        String responseString = "";
        try {
            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                responseString = response.body().string();
            }
        } catch (IOException e) {
            Log.e("Database Helper", "Connection Error: " + e.toString());
            return false;
        } catch (NullPointerException e) {
            Log.e("Database Helper", "Null Pointer Exception: " + e.toString());
        }
        JSONArray json;
        try {
            json = new JSONArray(responseString);
        } catch (JSONException e) {
            Log.e("Database Helper: ", "Error parsing JSON response: " + e.toString());
            return false;
        }
        if (json == null) {
            Log.e("Database Helper", "Response JSON has null value");
            return false;
        }
        ArrayList<Poem> poems = fetchPoemsFromJsonResponse(json);
        ArrayList<Integer> poemIDsForUpload = null;
        if (poems.size() > 0) {
            addNewPoemsToLocalDB(poems);
            poemIDsForUpload = findPoemsForUpload(poems);
        }
        //Todo Upload Poems
        /*
        try {
            uploadPoemsToWP(poemIDsForUpload);
        } catch (IOException e) {

        }*/
        return true;
    }

    private void uploadPoemsToWP(ArrayList<Integer> poemIDsForUpload) {
        String[] projection = {
                PoemEntry.COLUMN_AUTHOR_NAME,
                PoemEntry.COLUMN_ORIGINAL_TITLE_NAME,
                PoemEntry.COLUMN_PUBLICATION_YEAR_NAME,
                PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME,
                PoemEntry.COLUMN_ORIGINAL_LANGUAGE_NAME
        };

        for (Integer i : poemIDsForUpload) {
            Cursor c = mresolver.query(ContentUris.withAppendedId(PoemEntry.CONTENT_URI, i), projection, null, null, null);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                //TODO handle upload of poem;
            }
            c.close();

        }
    }

    private void addNewPoemsToLocalDB(ArrayList<Poem> poems) {
        String[] projection = {PoemEntry.COLUMN_AUTHOR_NAME,
                PoemEntry.COLUMN_ORIGINAL_TITLE_NAME};
        String selection = PoemEntry.COLUMN_AUTHOR_NAME + "=?" + " AND " + PoemEntry.COLUMN_ORIGINAL_TITLE_NAME + "=?";
        for (int i = 0; i < poems.size(); i++) {
            Poem p = poems.get(i);
            String[] selectionArgs = {
                    p.getAuthor(),
                    p.getTitle()
            };

            Cursor c = mresolver.query(PoemEntry.CONTENT_URI, projection, selection, selectionArgs, null);
            if (c != null && c.getCount() == 0) {
                ContentValues values = new ContentValues();
                String lng = "0";
                if (p.getYear() > 0) {
                    values.put(PoemEntry.COLUMN_PUBLICATION_YEAR_NAME, p.getYear());
                }
                if (p.getLanguageID() > 0) {
                    values.put(PoemEntry.COLUMN_ORIGINAL_LANGUAGE_NAME, p.getLanguageID());
                }
                values.put(PoemEntry.COLUMN_AUTHOR_NAME, p.getAuthor());
                values.put(PoemEntry.COLUMN_ORIGINAL_TITLE_NAME, p.getTitle());
                values.put(PoemEntry.COLUMN_ORIGINAL_TEXTBODY_NAME, p.getPoemBody());
                values.put(PoemEntry.COLUMN_ORIGINAL_LANGUAGE_NAME, p.getLanguageID());
                Uri u = mresolver.insert(PoemEntry.CONTENT_URI, values);


            }
            c.close();
        }
    }

    private ArrayList<Integer> findPoemsForUpload(ArrayList<Poem> poems) {
        //Turn Author + title into Hashmap to make it easier to search in them
        HashMap<String, String> poemHash = new HashMap<>();

        //Query for all Poems in database
        ArrayList<Integer> ids = new ArrayList<>();
        String[] projection = {PoemEntry._ID,
                PoemEntry.COLUMN_ORIGINAL_TITLE_NAME,
                PoemEntry.COLUMN_AUTHOR_NAME};

        Cursor c = mresolver.query(PoemEntry.CONTENT_URI, projection, null, null, null);
        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                for (Poem p : poems) {
                    if (p.getAuthor().equals(c.getString(c.getColumnIndexOrThrow(PoemEntry.COLUMN_AUTHOR_NAME)))
                            && p.getTitle().equals(c.getString(c.getColumnIndexOrThrow(PoemEntry.COLUMN_ORIGINAL_TITLE_NAME)))) {
                        ids.add(c.getInt(c.getColumnIndexOrThrow(PoemEntry._ID)));
                    }

                }
            }
            c.close();
            if (ids.size() > 0) {
                return ids;
            }

        }
        return null;
    }

    private ArrayList<Poem> fetchPoemsFromJsonResponse(JSONArray json) {
        //TODO match Poems in JSON
        ArrayList<Poem> poems = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            try {
                JSONObject item = json.getJSONObject(i);
                String author = item.getJSONObject("acf").getString("author");
                String title = item.getJSONObject("title").getString("rendered");
                String textEnc = item.getJSONObject("content").getString("rendered");
                textEnc = textEnc.replace("\n", "<br/>");
                String text = Html.fromHtml(textEnc, Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH).toString();
                String language = item.getJSONObject("acf").getString("org_language");
                String year = item.getJSONObject("acf").getString("year");
                int lng;
                switch (language) {
                    case "Deutsch":
                        lng = PoemEntry.LANGUAGE_GERMAN;
                        break;
                    case "Englisch":
                        lng = PoemEntry.LANGUAGE_ENGLISH;
                        break;

                    case "Italienisch":
                        lng = PoemEntry.LANGUAGE_ITALIAN;
                        break;

                    case "Spanisch":
                        lng = PoemEntry.LANGUAGE_SPANISH;
                        break;

                    case "Russisch":
                        lng = PoemEntry.LANGUAGE_RUSSIAN;
                        break;

                    case "Französisch":
                        lng = PoemEntry.LANGUAGE_FRENCH;
                        break;

                    default:
                        lng = 0;

                }

                if (!year.equals("")) {
                    int yr = Integer.valueOf(year);
                    poems.add(new Poem(i, author, title, text, yr, lng));
                } else {
                    poems.add(new Poem(i, author, title, text, lng));
                }


            } catch (JSONException e) {
                Log.e("Wordpress Helper", "Error Parsing JSON to Poems: " + e.toString());
            }

        }
        if (poems.size() > 0) {
            return poems;
        } else {
            return null;
        }
    }


}


