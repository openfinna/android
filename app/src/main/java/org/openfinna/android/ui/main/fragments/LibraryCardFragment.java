/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.openfinna.android.R;
import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.utils.AuthUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class LibraryCardFragment extends KirkesFragment {


    public LibraryCardFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View inflate = inflater.inflate(R.layout.fragment_libcard, null, false);
        return inflate;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        getActivity().getWindow().setAttributes(lp);
        ImageView barcode = view.findViewById(R.id.barcode);
        TextView signature = view.findViewById(R.id.signature);
        TextView cardNum = view.findViewById(R.id.cardNumber);
        try {
            LoginUser user = AuthUtils.getAuthentication(getContext());
            assert user != null;
            cardNum.setText(user.getUserAuthentication().getUsername());


            if (user.getUser().getLibraryPreferences() != null && user.getUser().getLibraryPreferences().getFirstName() != null)
                signature.setText(user.getUser().getLibraryPreferences().getFirstName());
            else
                signature.setText(user.getUser().getName());

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(user.getUserAuthentication().getUsername(), BarcodeFormat.CODE_39, 900, 120);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                barcode.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
                snack(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            snack(e.getMessage());
        }

    }

}
