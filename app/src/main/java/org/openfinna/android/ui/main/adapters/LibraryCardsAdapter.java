/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.openfinna.android.R;
import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.main.adapters.viewholders.LibraryCardViewHolder;

import java.util.List;

public class LibraryCardsAdapter extends RecyclerView.Adapter<LibraryCardViewHolder> {


    private LibraryCardsActionsInterface listener;
    private List<LoginUser> cards;

    public LibraryCardsAdapter(LibraryCardsActionsInterface listener, List<LoginUser> cards) {
        this.listener = listener;
        this.cards = cards;
    }

    @NonNull
    @Override
    public LibraryCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LibraryCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryCardViewHolder holder, int position) {
        holder.onBind(cards.get(position), position, this, listener);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public interface LibraryCardsActionsInterface {
        void onCardClick(LoginUser user);

        void onDeleteRequest(LoginUser user, LibraryCardsAdapter adapter);
    }
}
