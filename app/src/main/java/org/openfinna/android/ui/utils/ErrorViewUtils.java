/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.utils;

import android.view.View;
import android.widget.TextView;

import org.openfinna.android.R;

public class ErrorViewUtils {


    public static void setError(String errorReason, View errorView) {
        TextView errorReasonText = errorView.findViewById(R.id.errorReason);
        errorReasonText.setText(errorReason);
        errorView.setVisibility(View.VISIBLE);
    }

    public static void hideError(View errorView) {
        errorView.setVisibility(View.GONE);
    }
}
