/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.adapters.viewholders;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.openfinna.android.R;
import org.openfinna.android.classes.HomepageAction;
import org.openfinna.android.classes.HomepageCard;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class HomepageNotificationViewHolder extends RecyclerView.ViewHolder {

    private TextView title, desc;
    private ImageView imageView;
    private LinearLayout buttons;

    public HomepageNotificationViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.notifTitle);
        desc = itemView.findViewById(R.id.notifDesc);
        imageView = itemView.findViewById(R.id.notifIcon);
        buttons = itemView.findViewById(R.id.buttons);
    }

    public void onBind(final HomepageCard card, final int index, final ActionsListener listener) {
        title.setText(card.getHomepageCardTitle());
        desc.setText(Html.fromHtml(card.getHomepageCardDesc()));
        imageView.setImageResource(card.getRes());
        if (card.getIcon() != null) {
            imageView.setImageBitmap(card.getIcon());
        }
        if (card.getIconURL() != null && !card.getIconURL().isEmpty()) {
            Picasso.get().load(card.getIconURL()).into(imageView);
        }
        buttons.removeAllViews();
        for (final HomepageAction action : card.getActions()) {
            Button button = new Button(itemView.getContext());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onActionClick(action.getActionID(), index, card);
                }
            });
            button.setText(action.getText());
            button.setBackgroundColor(Color.TRANSPARENT);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            Random rand = new Random();
            params.setMargins(1, 0, 2, 0);
            button.setId(rand.nextInt(10000));
            button.setLayoutParams(params);

            buttons.addView(button);
        }

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

    public interface ActionsListener {
        void onActionClick(String actionID, int index, HomepageCard card);
    }
}
