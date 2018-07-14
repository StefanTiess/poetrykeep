package de.stefantiess.poetrykeep;

public class Poem {
    private int mId;
    private String mAuthor;
    private String mTitle;
    private String mPoemBody;
    private int mLanguageID;
    private int mYear;

    public Poem(int id, String author, String title, String poemBody, int year, int languageID) {
        mAuthor = author;
        mTitle = title;
        mLanguageID = languageID;
        mPoemBody = poemBody;
        mYear = year;
        mId = id;
    }


    public Poem(int id, String author, String title, String poemBody, int languageID) {
        mAuthor = author;
        mTitle = title;
        mLanguageID = languageID;
        mPoemBody = poemBody;
        mYear = 0;
        mId = id;

    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getPoemBody() {
        return mPoemBody;
    }

    public void setPoemBody(String mPoemBody) {
        this.mPoemBody = mPoemBody;
    }

    public int getLanguageID() {
        return mLanguageID;
    }

    public void setLanguageID(int mLanguageID) {
        this.mLanguageID = mLanguageID;
    }

    public int getYear() {
        return mYear;
    }

    public int getID() {
        return mId;
    }

    public void setYear(int mYear) {
        this.mYear = mYear;
    }
}
