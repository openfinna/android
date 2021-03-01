/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.openfinna.android.R;
import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.main.adapters.DetailsTabAdapter;
import org.openfinna.android.ui.main.fragments.infotabs.DescriptionTab;
import org.openfinna.android.ui.main.fragments.infotabs.InformationTab;
import org.openfinna.android.ui.main.fragments.infotabs.TagsTab;
import org.openfinna.android.ui.utils.ErrorViewUtils;
import org.openfinna.java.connector.classes.ResourceInfo;
import org.openfinna.java.connector.classes.models.Resource;
import org.openfinna.java.connector.interfaces.ResourceInfoInterface;

public class BookInfoActivity extends KirkesActivity implements ResourceInfoInterface {


    private View content, error, progress;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DetailsTabAdapter detailsTabAdapter;
    private ResourceInfo resourceInfo;
    private SwipeRefreshLayout swipeLayout;
    private ImageView book_cover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        resourceInfo = (ResourceInfo) getIntent().getSerializableExtra("resourceInfo");
        Resource resource = (Resource) getIntent().getSerializableExtra("resource");
        content = findViewById(R.id.content);
        progress = findViewById(R.id.progress);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewpager);

        progress.setVisibility(View.VISIBLE);
        error = findViewById(R.id.errorLayout);
        detailsTabAdapter = new DetailsTabAdapter(getSupportFragmentManager());
        viewPager.setAdapter(detailsTabAdapter);
        tabLayout.setupWithViewPager(viewPager);
        swipeLayout = findViewById(R.id.swipeLayout);
        book_cover = findViewById(R.id.book_cover);
        book_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageViewIntent = new Intent(BookInfoActivity.this, BookInfoImageFullscreen.class);
                imageViewIntent.putExtra("resource", resourceInfo);
                startActivity(imageViewIntent);
            }
        });
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String resourceId;
                if (resourceInfo == null) {
                    resourceId = getIntent().getStringExtra("resourceId");

                } else if (resource != null) {
                    resourceId = resource.getId();
                } else {
                    resourceId = resourceInfo.getId();
                }
                Log.e("RID", "RID: " + resourceId);
                finnaClient.resourceInfo(resourceId, BookInfoActivity.this);
            }
        });
        if (resourceInfo == null) {
            String book_id = getIntent().getStringExtra("resourceId");
            ErrorViewUtils.hideError(error);
            finnaClient.resourceInfo(book_id, this);
        } else {
            progress.setVisibility(View.GONE);
            ErrorViewUtils.hideError(error);
            content.setVisibility(View.VISIBLE);
            updateUI(resourceInfo);
        }
    }

    private void updateUI(ResourceInfo resourceInfo) {
        setTitle(resourceInfo.getTitle());
        Picasso.get().load(resourceInfo.getImageLink()).into(book_cover);
        detailsTabAdapter.clearFragments();
        detailsTabAdapter.addFragment(DescriptionTab.newInstance(resourceInfo), getString(R.string.description));
        detailsTabAdapter.addFragment(InformationTab.newInstance(resourceInfo), getString(R.string.book_details));
        detailsTabAdapter.addFragment(TagsTab.newInstance(resourceInfo), getString(R.string.tags));
        detailsTabAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResourceInfo(ResourceInfo resourceInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                ErrorViewUtils.hideError(error);
                progress.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);
                BookInfoActivity.this.resourceInfo = resourceInfo;
                updateUI(BookInfoActivity.this.resourceInfo);
            }
        });
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                content.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                ErrorViewUtils.setError(e.getMessage(), error);
            }
        });
    }
}
