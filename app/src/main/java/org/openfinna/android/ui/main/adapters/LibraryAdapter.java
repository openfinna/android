/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.openfinna.android.R;

import org.openfinna.android.ui.main.adapters.viewholders.LibraryViewHolder;
import org.openfinna.java.connector.FinnaClient;
import org.openfinna.java.connector.classes.models.libraries.Library;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryViewHolder> {

    private List<Library> libraries;
    private SelectListener listener;
    private FinnaClient finnaClient;

    public interface UpdateListener {
        void onUpdate(Library library, int pos);
    }

    public LibraryAdapter(List<Library> libraries, FinnaClient finnaClient) {
        this.libraries = libraries;
        this.finnaClient = finnaClient;
    }

    public LibraryAdapter(List<Library> libraries, SelectListener listener, FinnaClient finnaClient) {
        this.libraries = libraries;
        this.listener = listener;
        this.finnaClient = finnaClient;
    }

    @NonNull
    @Override
    public LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LibraryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.library, parent, false), listener, finnaClient);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
        holder.onBind(libraries.get(position), position, (library, pos) -> {
            Log.e("LA", new Gson().toJson(library.getImages()));
            libraries.set(pos, library);
        });
    }

    @Override
    public int getItemCount() {
        return libraries.size();
    }

    public interface SelectListener {
        void onSelect(Library library);
    }
}
