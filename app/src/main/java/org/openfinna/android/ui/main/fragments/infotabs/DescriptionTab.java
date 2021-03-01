/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.fragments.infotabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.openfinna.android.R;

import org.openfinna.java.connector.FinnaClient;
import org.openfinna.java.connector.classes.ResourceInfo;


public class DescriptionTab extends Fragment {

    private static final String ARG_BOOK = "book";

    private ResourceInfo book;
    private FinnaClient finnaClient;

    public DescriptionTab() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param book Book
     * @return A new instance of fragment DescriptionTab.
     */
    public static DescriptionTab newInstance(ResourceInfo book) {
        DescriptionTab fragment = new DescriptionTab();
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
        return inflater.inflate(R.layout.fragment_description_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView description = view.findViewById(R.id.description);

        /*if book.getDescription() != null && !Jsoup.parse(book.getDescription()).wholeText().isEmpty())
            description.setText(Jsoup.parse(book.getDescription()).wholeText());
        else*/
            description.setText(R.string.no_description);

    }
}
