/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.classes;

import com.google.gson.annotations.SerializedName;

import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.building.Building;

import java.io.Serializable;

public class LoginUser implements Serializable {

    @SerializedName("userAuth")
    private UserAuthentication userAuthentication;

    @SerializedName("user")
    private User user;

    @SerializedName("building")
    private Building building;

    public LoginUser(UserAuthentication userAuthentication, User user, Building building) {
        this.userAuthentication = userAuthentication;
        this.user = user;
        this.building = building;
    }

    public UserAuthentication getUserAuthentication() {
        return userAuthentication;
    }

    public void setUserAuthentication(UserAuthentication userAuthentication) {
        this.userAuthentication = userAuthentication;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }
}
