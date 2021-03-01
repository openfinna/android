/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.adapters.viewholders;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.openfinna.android.R;
import org.openfinna.android.ui.main.adapters.BibBooksAdapter;
import org.openfinna.java.connector.classes.ResourceInfo;
import org.openfinna.java.connector.classes.models.resource.Author;
import com.squareup.picasso.Picasso;

public class BibBookViewHolder extends RecyclerView.ViewHolder {

    private TextView bookName, bookAuthor, publishYear;
    private ImageView bookCover;
    private Button reserve;
    private int lastPosition = -1;


    public BibBookViewHolder(@NonNull View itemView) {
        super(itemView);
        bookAuthor = itemView.findViewById(R.id.bookAuthor);
        bookName = itemView.findViewById(R.id.bookName);
        bookCover = itemView.findViewById(R.id.bookCover);
        publishYear = itemView.findViewById(R.id.releaseYear);
        reserve = itemView.findViewById(R.id.reserve);
    }


    public void onBind(final ResourceInfo book, int index, final BibBooksAdapter.BookActionsInterface listener) {
        bookName.setText(book.getTitle());
        StringBuilder authors = new StringBuilder();
        int authors_size = book.getAuthors().size() - 1;
        for (Author bookAuthor : book.getAuthors()) {
            int author_index = book.getAuthors().indexOf(bookAuthor);
            authors.append(bookAuthor.getName().replace(",", ""));
            Log.e("BBVH", "I:" + author_index + "EI:" + authors_size);
            if (author_index != authors_size)
                authors.append(", ");
        }
        bookAuthor.setText(authors.toString());
        publishYear.setText(String.valueOf(book.getPublicationYear()));
        if (index == -1)
            reserve.setVisibility(View.GONE);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onBookClick(book);
            }
        });
        reserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onReserveRequest(book);
            }
        });
        Picasso.get().load(book.getImageLink()).into(bookCover);
    }
}
