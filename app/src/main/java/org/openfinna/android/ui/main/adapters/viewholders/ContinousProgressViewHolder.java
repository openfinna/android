/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.adapters.viewholders;

import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.openfinna.android.R;

public class ContinousProgressViewHolder extends RecyclerView.ViewHolder {


    private ProgressBar progressBar;


    public ContinousProgressViewHolder(@NonNull View itemView) {
        super(itemView);
        progressBar = itemView.findViewById(R.id.progressBar);
    }

    public void onBind() {
        progressBar.setVisibility(View.VISIBLE);
    }
}
