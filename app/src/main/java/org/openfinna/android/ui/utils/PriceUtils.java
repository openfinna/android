/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.utils;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class PriceUtils {

    public static String formatPrice(double price, String currency) {
        /*DecimalFormat decimalFormat = (DecimalFormat)
                NumberFormat.getNumberInstance();
        decimalFormat.applyPattern("##.###,##");
        return decimalFormat.format(price)+" "+currency;*/
        return doubleToPrice(price, currency.charAt(0));
    }

    static String doubleToPrice(double price, char curr) {
        // Define currency to be used


        // Format the string using a DecimalFormat
        Locale locale = new Locale("de", "DE");
        if (curr == '$')
            locale = new Locale("en", "US");
        DecimalFormatSymbols sym = new DecimalFormatSymbols(locale);
        sym.setGroupingSeparator('.');
        if (curr == '$')
            sym.setGroupingSeparator(',');
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        formatter.applyPattern("##,##0.00");
        formatter.setDecimalFormatSymbols(sym);

        String returnString = formatter.format(price);

        // Replace "00" after the comma with "-"
       /* if (returnString.endsWith("00")) {
            int i = returnString.lastIndexOf("00");
            returnString = new StringBuilder(returnString).replace(i, i + 2, "-").toString();
        }*/

        // Add space between currency-symbol and price

        returnString = returnString + " " + curr;

        Log.i("PRICE FORMATTING", "double that goes in [" + price + "]; string that comes out [" + returnString + "]");

        return returnString;
    }
}
