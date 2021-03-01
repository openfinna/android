/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.utils;

import org.openfinna.java.connector.classes.models.libraries.Library;
import org.openfinna.java.connector.classes.models.libraries.schedule.Day;

import java.util.Date;

public class LibUtils {

    public static Day getTodayOpenDay(Library library) {
        Date currentDate = new Date();
        Day foundDay = null;
        for (Day day : library.getDays()) {
            if (DateUtils.sameDay(currentDate, day.getDate())) {
                foundDay = day;
                break;
            }
        }
        return foundDay;
    }

}
