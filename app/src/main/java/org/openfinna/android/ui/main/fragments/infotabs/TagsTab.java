/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.fragments.infotabs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.openfinna.android.R;
import org.openfinna.android.ui.main.activities.SearchActivity;
import org.openfinna.android.ui.main.fragments.KirkesFragment;
import org.openfinna.java.connector.classes.ResourceInfo;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class TagsTab extends KirkesFragment {

    private static final String ARG_BOOK = "book";

    private ResourceInfo book;

    public TagsTab() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param book Book
     * @return A new instance of fragment DescriptionTab.
     */
    public static TagsTab newInstance(ResourceInfo book) {
        TagsTab fragment = new TagsTab();
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
        return inflater.inflate(R.layout.fragment_tags_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView tagsList = view.findViewById(R.id.tags_list);
        final ArrayAdapter<String> tagsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        tagsList.setAdapter(tagsAdapter);
        tagsAdapter.addAll(book.getTopics());
        tagsAdapter.notifyDataSetChanged();
        tagsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                builder.setTitle(R.string.search_tag);
                builder.setMessage(getString(R.string.search_tag_desc, tagsAdapter.getItem(position)));
                builder.setNegativeButton(R.string.cancel, null);
                builder.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent searchIntent = new Intent(getContext(), SearchActivity.class);
                        searchIntent.putExtra("query", tagsAdapter.getItem(position));
                        startActivity(searchIntent);
                    }
                });
                builder.show();
            }
        });


    }

}
