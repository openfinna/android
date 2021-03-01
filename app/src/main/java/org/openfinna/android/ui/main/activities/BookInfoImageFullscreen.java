/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import org.openfinna.android.R;

import org.openfinna.java.connector.classes.ResourceInfo;
import org.openfinna.java.connector.classes.models.Resource;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class BookInfoImageFullscreen extends KirkesActivity {


    private PhotoView imageView;
    private String url;
    private ProgressBar imageprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.content_book_info_image);
        Object book =  getIntent().getSerializableExtra("resource");
        if (book instanceof ResourceInfo)
            url = ((ResourceInfo) book).getImageLink();
        else if (book instanceof Resource)
            url = ((Resource) book).getImage();
        imageView = findViewById(R.id.full_image_view);
        imageprogress = findViewById(R.id.imageprogress);
        Picasso.get().load(url)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        imageprogress.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        imageprogress.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        snack(e.getMessage());
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

}
