/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.fragments.infotabs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.openfinna.android.R;
import org.openfinna.android.ui.main.fragments.KirkesFragment;

import org.jsoup.Jsoup;
import org.openfinna.java.connector.classes.ResourceInfo;
import org.openfinna.java.connector.classes.models.resource.Author;

import java.util.Locale;

import static android.content.Context.CLIPBOARD_SERVICE;


public class InformationTab extends KirkesFragment {

    private static final String ARG_BOOK = "book";

    private ResourceInfo book;

    public InformationTab() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param book Book
     * @return A new instance of fragment DescriptionTab.
     */
    public static InformationTab newInstance(ResourceInfo book) {
        InformationTab fragment = new InformationTab();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOOK, book);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = (ResourceInfo) getArguments().getSerializable(ARG_BOOK);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_information_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView title = view.findViewById(R.id.title);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(getString(R.string.book_title), title.getText().toString());
            }
        });
        if (book.getTitle() != null)
            title.setText(Jsoup.parse(book.getTitle()).wholeText());
        else
            title.setText(R.string.not_available);

        final TextView authors = view.findViewById(R.id.author);
        final TextView authors_text = view.findViewById(R.id.authors_text);
        authors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(authors_text.getText().toString(), authors.getText().toString());
            }
        });

        StringBuilder authors_string = new StringBuilder();

        int authors_size = book.getAuthors().size() - 1;
        for (Author bookAuthor : book.getAuthors()) {
            int author_index = book.getAuthors().indexOf(bookAuthor);
            authors_string.append(bookAuthor.getName().replace(",", ""));
            if (author_index == authors_size - 1)
                authors_string.append(" ").append(getString(R.string.and).toLowerCase()).append(" ");
            else if (author_index != authors_size)
                authors_string.append(", ");
        }

        final TextView language = view.findViewById(R.id.language);
        language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(getString(R.string.language), language.getText().toString());
            }
        });
        StringBuilder language_str = new StringBuilder();

        int lang_size = book.getLanguages().size() - 1;
        for (String lang : book.getLanguages()) {
            Locale lang_full = new Locale(lang);
            int lang_index = book.getLanguages().indexOf(lang);
            language_str.append(lang_full.getDisplayName());
            if (lang_index == lang_size - 1)
                language_str.append(" ").append(getString(R.string.and).toLowerCase()).append(" ");
            else if (lang_index != lang_size)
                language_str.append(", ");
        }
        if (language_str.length() > 1)
            language.setText(language_str.toString());
        else
            language.setText(R.string.not_available);

        if (book.getAuthors().size() < 2) {
            authors_text.setText(R.string.author);
        } else {
            authors_text.setText(R.string.authors);
        }
        if (authors_string.length() > 0)
            authors.setText(authors_string);
        else
            authors.setText(R.string.not_available);


        final TextView publisher = view.findViewById(R.id.publisher);
        publisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(getString(R.string.publisher), publisher.getText().toString());
            }
        });
        if (book.getPublishers().size() > 0)
            publisher.setText(book.getPublishers().get(0));
        else
            publisher.setText(R.string.not_available);

        final TextView published_date = view.findViewById(R.id.published_date);
        published_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(getString(R.string.published_date_text), published_date.getText().toString());
            }
        });


        if (book.getPublicationYear() != 1970)
            published_date.setText(String.valueOf(book.getPublicationYear()));
        else
            published_date.setText(R.string.not_available);

        final TextView call_numbers = view.findViewById(R.id.call_numbers);
        call_numbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(getString(R.string.call_numbers), call_numbers.getText().toString());
            }
        });

        if (book.getYkl() != null && !book.getYkl().isEmpty()) {
            StringBuilder cn_string = new StringBuilder();
            int cn_size = book.getYkl().size() - 1;
            for (String cn : book.getYkl()) {
                int author_index = book.getYkl().indexOf(cn);
                cn_string.append(cn);
                if (author_index != cn_size)
                    cn_string.append(", ");
            }
            call_numbers.setText(cn_string.toString());
        } else
            call_numbers.setText(R.string.not_available);


        final TextView isbn = view.findViewById(R.id.isbn);
        isbn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(getString(R.string.isbn), isbn.getText().toString());
            }
        });
        if (book.getIsbn() != null && !book.getIsbn().isEmpty())
            isbn.setText(book.getIsbn());
        else
            isbn.setText(R.string.not_available);


        final TextView type = view.findViewById(R.id.type);
        type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(getString(R.string.content_type_text), type.getText().toString());
            }
        });

        if (book.getFormats().size() > 0)
            type.setText(book.getFormats().get(0).getTranslated());
        else
            type.setText(R.string.not_available);

    }

    private void copyToClipboard(String label, String clipboardData) {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, clipboardData);
        clipboard.setPrimaryClip(clip);
        snack(getString(R.string.copied_to_clipboard, label));
    }
}
