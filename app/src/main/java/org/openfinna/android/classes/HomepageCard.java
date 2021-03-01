/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class HomepageCard {

    public static int TYPE_NOTIFICATION = 1;
    public static int TYPE_EMPTY_DUMMY = 2;
    public static int TYPE_LIBRARY_NOTIFICATION = 3;

    private String homepageCardTitle;
    private String homepageCardDesc;
    private String icon;
    private String iconURL;
    private int res;
    private int type;
    private Object payload;
    private List<HomepageAction> actions = new ArrayList<>();
    private boolean actionsEnabled = !actions.isEmpty();

    public HomepageCard(String homepageCardTitle, String homepageCardDesc, Bitmap icon, int type) {
        this.homepageCardTitle = homepageCardTitle;
        this.homepageCardDesc = homepageCardDesc;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        this.icon = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        this.type = type;
    }

    public HomepageCard(String homepageCardTitle, String homepageCardDesc, int icon, int type) {
        this.homepageCardTitle = homepageCardTitle;
        this.homepageCardDesc = homepageCardDesc;
        this.res = icon;
        this.type = type;
    }

    public HomepageCard(String homepageCardTitle, String homepageCardDesc, int icon, int type, List<HomepageAction> actions) {
        this.homepageCardTitle = homepageCardTitle;
        this.homepageCardDesc = homepageCardDesc;
        this.res = icon;
        this.type = type;
        this.actions = actions;
    }

    public HomepageCard(String homepageCardTitle, String homepageCardDesc, String iconURL, int type) {
        this.homepageCardTitle = homepageCardTitle;
        this.homepageCardDesc = homepageCardDesc;
        this.iconURL = iconURL;
        this.type = type;
    }

    public HomepageCard(String homepageCardTitle, String homepageCardDesc, Object payload, String iconURL, List<HomepageAction> actions, int type) {
        this.homepageCardTitle = homepageCardTitle;
        this.homepageCardDesc = homepageCardDesc;
        this.iconURL = iconURL;
        this.type = type;
        this.payload = payload;
        this.actions = actions;
        actionsEnabled = true;
    }

    public HomepageCard(String homepageCardTitle, String homepageCardDesc, Object payload, String iconURL, int type) {
        this.homepageCardTitle = homepageCardTitle;
        this.homepageCardDesc = homepageCardDesc;
        this.iconURL = iconURL;
        this.type = type;
        this.payload = payload;
    }

    public HomepageCard(String homepageCardTitle, String homepageCardDesc, String iconURL, int type, List<HomepageAction> actions) {
        this.homepageCardTitle = homepageCardTitle;
        this.homepageCardDesc = homepageCardDesc;
        this.iconURL = iconURL;
        this.type = type;
        this.actions = actions;
    }


    public HomepageCard(String homepageCardTitle, String homepageCardDesc, Bitmap icon, int type, Object payload) {
        this.homepageCardTitle = homepageCardTitle;
        this.homepageCardDesc = homepageCardDesc;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        this.icon = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        this.type = type;
        this.payload = payload;
    }

    public HomepageCard(String homepageCardTitle, String homepageCardDesc, int type, Object payload) {
        this.homepageCardTitle = homepageCardTitle;
        this.homepageCardDesc = homepageCardDesc;
        this.type = type;
        this.payload = payload;
    }

    public String getHomepageCardTitle() {
        return homepageCardTitle;
    }

    public void setHomepageCardTitle(String homepageCardTitle) {
        this.homepageCardTitle = homepageCardTitle;
    }

    public String getHomepageCardDesc() {
        return homepageCardDesc;
    }

    public void setHomepageCardDesc(String homepageCardDesc) {
        this.homepageCardDesc = homepageCardDesc;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Bitmap getIcon() {
        if (icon != null) {
            byte[] decodedString = Base64.decode(icon, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } else
            return null;
    }

    public void setIcon(Bitmap icon) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        this.icon = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRes() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }


    public List<HomepageAction> getActions() {
        return actions;
    }

    public void setActions(List<HomepageAction> actions) {
        this.actions = actions;
    }

    public boolean isActionsEnabled() {
        return actionsEnabled;
    }

    public void setActionsEnabled(boolean actionsEnabled) {
        this.actionsEnabled = actionsEnabled;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }
}
