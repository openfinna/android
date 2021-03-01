/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.adapters.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.openfinna.android.R;
import org.openfinna.android.ui.utils.PriceUtils;
import org.openfinna.java.connector.classes.models.fines.Fine;
import org.openfinna.java.connector.classes.models.fines.Fines;

import java.text.DateFormat;

public class FineViewHolder extends RecyclerView.ViewHolder {

    private TextView fine_description, price, fine_reg_date;
    private View divider;

    public FineViewHolder(@NonNull View itemView) {
        super(itemView);
        fine_description = itemView.findViewById(R.id.fine_desc);
        price = itemView.findViewById(R.id.fine_price);
        fine_reg_date = itemView.findViewById(R.id.fine_reg_date);
    }

    public void bind(Fine fine, Fines fineDetails) {
        fine_description.setText(fine.getDescription());
        fine_reg_date.setText(DateFormat.getDateInstance().format(fine.getRegistrationDate()));
        price.setText(PriceUtils.formatPrice(fine.getPrice(), fineDetails.getCurrency()));
    }
}
