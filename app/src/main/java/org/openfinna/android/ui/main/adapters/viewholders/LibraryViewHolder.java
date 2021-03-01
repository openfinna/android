/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.adapters.viewholders;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
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

import org.openfinna.android.ui.main.adapters.LibraryAdapter;
import org.openfinna.java.connector.FinnaClient;
import org.openfinna.java.connector.classes.models.libraries.Library;
import org.openfinna.java.connector.classes.models.libraries.schedule.Day;
import org.openfinna.java.connector.interfaces.LibrariesInterface;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class LibraryViewHolder extends RecyclerView.ViewHolder {

    private TextView libraryName, libraryCurrentlyOpen, todayOpeningTimesText;
    private ImageView libraryImage;
    private LibraryAdapter.SelectListener listener;
    private int lastPosition = -1;
    private FinnaClient finnaClient;


    public LibraryViewHolder(@NonNull View itemView, LibraryAdapter.SelectListener listener, FinnaClient finnaClient) {
        super(itemView);
        libraryName = itemView.findViewById(R.id.libraryName);
        libraryCurrentlyOpen = itemView.findViewById(R.id.libraryCurrentlyOpen);
        todayOpeningTimesText = itemView.findViewById(R.id.todayOpeningTimes);
        libraryImage = itemView.findViewById(R.id.libraryImage);
        this.listener = listener;
        this.finnaClient = finnaClient;
    }

    @Deprecated
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

    private void setAnimation2(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(new Random().nextInt(501));//to make duration random number between [0,501)
            viewToAnimate.startAnimation(anim);
            lastPosition = position;
        }
    }

    public void onBind(final Library library, int index) {

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onSelect(library);
            }
        });
        libraryName.setText(library.getName());
        Day todayOpeningTimes = new Day(new Date(), true, null);
        for (Day today : library.getDays()) {
            Calendar todayC = Calendar.getInstance();
            todayC.setTime(today.getDate());
            if (todayC.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) && todayC.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                todayOpeningTimes = today;
                break;
            }
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        libraryCurrentlyOpen.setText(!todayOpeningTimes.isClosed() ? itemView.getContext().getString(R.string.lib_open) : itemView.getContext().getString(R.string.lib_closed));

        if (todayOpeningTimes.getSchedule() == null || todayOpeningTimes.isClosed() || todayOpeningTimes.getSchedule().getOpens() == null)
            todayOpeningTimesText.setVisibility(View.GONE);
        else {
            todayOpeningTimesText.setVisibility(View.VISIBLE);

            todayOpeningTimesText.setText(itemView.getContext().getString(R.string.lib_open_times, simpleDateFormat.format(todayOpeningTimes.getSchedule().getOpens()), simpleDateFormat.format(todayOpeningTimes.getSchedule().getCloses())));
        }
        libraryImage.setVisibility(View.VISIBLE);
        libraryImage.setVisibility(View.GONE);

        if (library.getImages() != null && !library.getImages().isEmpty())
            Picasso.get().load(library.getImages().get(0).getUrl()).into(libraryImage);
        else {
            finnaClient.getLibrary(library, new LibrariesInterface() {
                @Override
                public void onGetLibraries(List<Library> list) {

                }

                @Override
                public void onGetLibrary(Library library) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (listener != null)
                                        listener.onSelect(library);
                                }
                            });
                            if (library.getImages() != null && !library.getImages().isEmpty()) {
                                libraryImage.setVisibility(View.VISIBLE);
                                Picasso.get().load(library.getImages().get(0).getUrl()).into(libraryImage);
                            }
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }

        if (index != -1)
            setAnimation2(itemView, index);
    }

}
