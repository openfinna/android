/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.adapters.viewholders;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.openfinna.android.R;
import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.main.adapters.LibraryCardsAdapter;

public class LibraryCardViewHolder extends RecyclerView.ViewHolder {

    private TextView username, cardNumber;
    private View deleteCard;

    public LibraryCardViewHolder(@NonNull View itemView) {
        super(itemView);
        username = itemView.findViewById(R.id.username);
        cardNumber = itemView.findViewById(R.id.cardName);
        deleteCard = itemView.findViewById(R.id.deleteCard);
    }

    private void setAnimation(View itemView, int i) {
        boolean isNotFirstItem = i == -1;
        i++;
        itemView.setAlpha(0.f);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animator = ObjectAnimator.ofFloat(itemView, "alpha", 0.f, 0.5f, 1.0f);
        ObjectAnimator.ofFloat(itemView, "alpha", 0.f).start();
        animator.setStartDelay(isNotFirstItem ? 300 / 2 : (i * 300 / 3));
        animator.setDuration(300);
        animatorSet.play(animator);
        animator.start();
    }

    public void onBind(final LoginUser loginUser, int index, final LibraryCardsAdapter adapter, final LibraryCardsAdapter.LibraryCardsActionsInterface listener) {
        username.setText(loginUser.getUser().getName());
        cardNumber.setText(loginUser.getUserAuthentication().getUsername());
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCardClick(loginUser);
            }
        });
        deleteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteRequest(loginUser, adapter);
            }
        });
        setAnimation(itemView, index);
    }
}
