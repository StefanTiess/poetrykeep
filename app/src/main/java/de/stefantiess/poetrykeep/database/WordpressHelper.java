package de.stefantiess.poetrykeep.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.stefantiess.poetrykeep.Poem;
import de.stefantiess.poetrykeep.database.PoemContract.PoemEntry;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class WordpressHelper {

    private static final String BASE_URL = "http://poetrykeep.stefantiess.de/wp-json/wp/v2/";
    private static final String PARAMETER_GET_ALL_POSTS = "posts";
    private static final String PARAMETER_GET_ALL_AUTHORS = "poet";
    private static final String TAG = "Wordpress Helper";
    JSONArray authorJSON = null;
    SparseArray authorsList = new SparseArray();
    private ContentResolver mresolver;
    private int totalPages = 0;
    private int currentPage = 1;


    public WordpressHelper(ContentResolver provider) {
        mresolver = provider;
    }

    public boolean syncDatabase() {
        getAuthorList();
        getAllPoems();
        //Todo Upload Poems
        Log.d("WordpressHelper", authorJSON.toString());

        /*
        try {
            uploadPoemsToWP(poemIDsForUpload);
        } catch (IOException e) {

        }*/
        return true;
    }

    private void getAuthorList() {
        String url = BASE_URL + PARAMETER_GET_ALL_AUTHORS;
        authorJSON = queryWorpressAPI(url);

        for (int i = 0; i < authorJSON.length(); i++) {
            try {
                JSONObject item = authorJSON.getJSONObject(i);
                authorsList.put(item.getInt("id"), item.getString("name"));


            } catch (JSONException e) {
                Log.e("Wordpress Helper", "Error Parsing AuthorJSON to AuthorList: " + e.toString());
            }
        }

    }


    private void getAllPoems() {
        String url = BASE_URL + PARAMETER_GET_ALL_POSTS;
        ArrayList<Poem> poems;

        JSONArray json = queryWorpressAPI(url);
        poems = fetchPoemsFromJsonResponse(json);
        if (poems.size() > 0) {
            addNewPoemsToLocalDB(poems);
        }


    }

    private JSONArray queryWorpressAPI(String url) {
        OkHttpClient client = new OkHttpClient();
        //JSONArray for results;
        JSONArray json = new JSONArray();
        //Pagination defaults
        currentPage = 1;
        totalPages = 1;
        while (currentPage <= totalPages) {
            Request request = new Request.Builder().url(url + "?page=" + currentPage).build();
            String responseString = "";
            try {
                Response response = client.newCall(request).execute();
                if (response.body() != null) {
                    responseString = response.body().string();
                } else {
                    Log.e(TAG, "Response Body from API Request is null");
                }
                if (response.headers() != null) {
                    Headers headers = response.headers();
                    totalPages = Integer.parseInt(headers.get("X-WP-TotalPages"));
                    if (totalPages < 1) return null;

                }

            } catch (IOException e) {
                Log.e(TAG, "Connection Error: " + e.toString());
                return null;
            } catch (NullPointerException e) {
                Log.e(TAG, "Null Pointer Exception: " + e.toString());
            }

            try {
                JSONArray partialArray = new JSONArray(responseString);
                if (partialArray.length() > 0) {
                    for (int i = 0; i < partialArray.length(); i++) {
                        json.put(partialArray.getJSONObject(i));
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON response: " + e.toString());
                return null;
            }
            currentPage++;

        }
        return json;
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
            if (c != null) {
                c.close();
            }

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
            if (c != null) {
                c.close();
            }
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
                //try to get the author either from taxonomy or author field.
                int authorID = item.getJSONArray("poet").getInt(0);
                String author = String.valueOf(authorsList.get(authorID));
                if (author == null) author = item.getJSONObject("acf").getString("author");
                if (author == null) break;
                String title = item.getJSONObject("title").getString("rendered");
                String textEnc = item.getJSONObject("content").getString("rendered");
                //textEnc = textEnc.replace("\n", "<br/>");
                String text = Html.fromHtml(textEnc, Html.FROM_HTML_MODE_LEGACY).toString();
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


