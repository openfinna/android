/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.adapters.viewholders;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.openfinna.android.R;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.squareup.picasso.Picasso;

import org.openfinna.android.ui.main.adapters.ReservationAdapter;
import org.openfinna.android.ui.utils.StatusUtils;
import org.openfinna.java.connector.classes.models.holds.Hold;
import org.openfinna.java.connector.classes.models.holds.HoldStatus;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

public class BookHoldViewHolder extends RecyclerView.ViewHolder {

    private TextView bookName, bookAuthor, expireDate, mediaType, status, reservation_number, queue, pickup_location;
    private ImageView bookCover;
    private CardView root;
    private Button cancel, edit;
    private int lastPosition = -1;

    public BookHoldViewHolder(@NonNull View itemView) {
        super(itemView);
        bookAuthor = itemView.findViewById(R.id.bookAuthor);
        bookName = itemView.findViewById(R.id.bookName);
        bookCover = itemView.findViewById(R.id.bookCover);
        expireDate = itemView.findViewById(R.id.expireDate);
        mediaType = itemView.findViewById(R.id.mediaType);
        status = itemView.findViewById(R.id.status);
        root = itemView.findViewById(R.id.cardBG);
        cancel = itemView.findViewById(R.id.cancel);
        edit = itemView.findViewById(R.id.edit);
        queue = itemView.findViewById(R.id.queue);
        pickup_location = itemView.findViewById(R.id.pickup_location);
        reservation_number = itemView.findViewById(R.id.reservation_number);
    }

    private static int getContrastColor(int color) {
        double y = (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.blue(color)) / 1000;
        return y >= 128 ? Color.BLACK : Color.WHITE;
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

    public void onBind(final Hold book, int index, final ReservationAdapter.BookActionsInterface listener) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        bookName.setText(book.getResource().getTitle());
        bookAuthor.setText(book.getResource().getAuthor());
        if (index != -1)
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onBookClick(book);
                }
            });
        expireDate.setVisibility((book.getHoldDate() != null) ? View.VISIBLE : View.GONE);
        if (book.getHoldDate() != null)
            expireDate.setText(itemView.getContext().getString(R.string.order_date, dateFormat.format(book.getHoldDate())));
        expireDate.setVisibility((book.getExpirationDate() != null) ? View.VISIBLE : View.GONE);
        if (book.getExpirationDate() != null) {
            if (book.getHoldStatus() == HoldStatus.AVAILABLE) {
                expireDate.setText(itemView.getContext().getString(R.string.last_pickup_day, dateFormat.format(book.getExpirationDate())));
            } else {
                expireDate.setText(itemView.getContext().getString(R.string.expiration_day, dateFormat.format(book.getExpirationDate())));
            }
        }
        mediaType.setText(itemView.getContext().getString(R.string.content_type, book.getResource().getType()));

        status.setText(StatusUtils.statusToString(itemView.getContext(), book.getHoldStatus()));

        queue.setVisibility((book.getQueue() > 0) ? View.VISIBLE : View.GONE);
        queue.setText(itemView.getContext().getString(R.string.queue, String.valueOf(book.getQueue())));
        boolean pickup_show = (book.getHoldPickupData() != null && book.getHoldPickupData().getPickupLocation() != null);
        pickup_location.setVisibility(pickup_show ? View.VISIBLE : View.GONE);
        if (pickup_show)
            pickup_location.setText(itemView.getContext().getString(R.string.pickup_location_entry_hold, book.getHoldPickupData().getPickupLocation()));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onReservationCancel(book);
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onReservationEdit(book);
            }
        });
        cancel.setVisibility(book.isCancellable() ? View.VISIBLE : View.GONE);
        edit.setVisibility(book.isCancellable() ? View.VISIBLE : View.GONE);
        reservation_number.setVisibility(book.getHoldStatus() == HoldStatus.AVAILABLE ? View.VISIBLE : View.GONE);
        if (book.getHoldStatus() == HoldStatus.AVAILABLE) {
            cancel.setVisibility(View.GONE);
            int color = itemView.getContext().getResources().getColor(R.color.green_available);
            root.setCardBackgroundColor(color);
            bookAuthor.setTextColor(getContrastColor(color));
            bookName.setTextColor(getContrastColor(color));
            expireDate.setTextColor(getContrastColor(color));
            mediaType.setTextColor(getContrastColor(color));
            status.setTextColor(getContrastColor(color));
            reservation_number.setTextColor(getContrastColor(color));
            queue.setTextColor(getContrastColor(color));
            pickup_location.setTextColor(getContrastColor(color));
            status.setText(R.string.ready_for_pickup);
            reservation_number.setText(itemView.getContext().getString(R.string.reservation_num, NumberFormat.getNumberInstance().format(book.getHoldPickupData().getReservationNumber())));
        } else {
            int color = root.getCardBackgroundColor().getDefaultColor();
            root.setCardBackgroundColor(color);
            bookAuthor.setTextColor(getContrastColor(color));
            bookName.setTextColor(getContrastColor(color));
            expireDate.setTextColor(getContrastColor(color));
            mediaType.setTextColor(getContrastColor(color));
            status.setTextColor(getContrastColor(color));
            reservation_number.setTextColor(getContrastColor(color));
            pickup_location.setTextColor(getContrastColor(color));
            queue.setTextColor(getContrastColor(color));
        }

        Glide.with(itemView.getContext())
                .asBitmap()
                .load(book.getResource().getImage())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        int width = resource.getWidth();
                        int height = resource.getHeight();
                        bookCover.setVisibility((width <= 10 && height <= 10) ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
        Picasso.get().load(book.getResource().getImage()).into(bookCover);
        if (index != -1)
            setAnimation2(itemView, index);
    }

}
