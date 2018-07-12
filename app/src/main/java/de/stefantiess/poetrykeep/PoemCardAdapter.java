package de.stefantiess.poetrykeep;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PoemCardAdapter extends ArrayAdapter<Poem> {

    Poem mPoem;

    public PoemCardAdapter(@NonNull Context context, @NonNull List<Poem> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView == null) {
            Context context = getContext();
            listItemView = LayoutInflater.from(context).inflate(R.layout.poem_preview_card, parent, false);
        }

        final Poem poem = getItem(position);
        TextView body = listItemView.findViewById(R.id.poemcard_text_preview_view);
        body.setText(poem.getPoemBody());

        TextView author = listItemView.findViewById(R.id.poemcard_author_view);
        author.setText(poem.getAuthor());

        TextView title = listItemView.findViewById(R.id.poemcard_title_view);
        title.setText(poem.getTitle());

        TextView year = listItemView.findViewById(R.id.poemcard_year_view);
        if (poem.getYear() > 0) {
            year.setText(String.valueOf(poem.getYear()));
        }
        else year.setText("");

        return listItemView;
    }
}
