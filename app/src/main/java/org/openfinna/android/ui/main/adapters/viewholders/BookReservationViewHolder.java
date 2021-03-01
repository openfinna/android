/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.adapters.viewholders;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.openfinna.android.R;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.openfinna.android.ui.main.adapters.ReservationAdapter;
import org.openfinna.java.connector.classes.models.loans.Loan;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

public class BookReservationViewHolder extends RecyclerView.ViewHolder {

    private TextView bookName, bookAuthor, expireDate, renew_count;
    private ImageView bookCover, renew;
    private int lastPosition = -1;

    public BookReservationViewHolder(@NonNull View itemView) {
        super(itemView);
        bookAuthor = itemView.findViewById(R.id.bookAuthor);
        bookName = itemView.findViewById(R.id.bookName);
        bookCover = itemView.findViewById(R.id.bookCover);
        expireDate = itemView.findViewById(R.id.expireDate);
        renew = itemView.findViewById(R.id.renew);
        renew_count = itemView.findViewById(R.id.renew_count);
    }

    @Deprecated
    private void setAnimation(View itemView, int i) {
        boolean isNotFirstItem = i == -1;
        i++;
        itemView.setAlpha(0.f);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animator = ObjectAnimator.ofFloat(itemView, "alpha", 0.f, 0.5f, 1.0f);
        ObjectAnimator.ofFloat(itemView, "alpha", 0.f).start();
        animator.setStartDelay(isNotFirstItem ? 500 / 2 : (i * 500 / 3));
        animator.setDuration(500);
        animatorSet.play(animator);
        animator.start();
    }

    private void setAnimation2(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(new Random().nextInt(501));//to make duration random number between [0,501)
            viewToAnimate.startAnimation(anim);
            lastPosition = position;
        }
    }

    public void onBind(final Loan book, int index, final ReservationAdapter.BookActionsInterface listener) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        bookName.setText(book.getResource().getTitle());
        bookAuthor.setText(book.getResource().getAuthor());
        renew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onReservationRenew(book);
            }
        });
        renew.setVisibility(book.getRenewId() == null ? View.GONE : View.VISIBLE);
        expireDate.setText(itemView.getContext().getString(R.string.return_day, dateFormat.format(book.getDueDate())));
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLoanClick(book);
            }
        });
        renew_count.setVisibility((book.getRenewsUsed() != 0) ? View.VISIBLE : View.GONE);
        if (book.getRenewsUsed() != 0)
            renew_count.setText(itemView.getContext().getString(R.string.renew_count, String.valueOf(book.getRenewsUsed()), String.valueOf(book.getRenewsTotal())));
        Picasso.get().load(book.getResource().getImage()).into(bookCover);
        //setAnimation2(itemView, index);
    }
}
