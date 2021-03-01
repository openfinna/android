/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.utils;

import android.content.Context;

import org.openfinna.android.R;
import org.openfinna.java.connector.classes.models.holds.HoldStatus;

public class StatusUtils {

    public static String statusToString(Context context, HoldStatus holdingStatus) {
        if (holdingStatus == HoldStatus.WAITING) {
            return context.getString(R.string.waiting_for_transport);
        } else if (holdingStatus == HoldStatus.IN_TRANSIT) {
            return context.getString(R.string.in_transit);
        } else if (holdingStatus == HoldStatus.AVAILABLE) {
            return context.getString(R.string.available);
        }
        return "";
    }
}
