/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.classes;

import com.google.gson.annotations.SerializedName;

import org.openfinna.java.connector.classes.models.holds.Hold;
import org.openfinna.java.connector.classes.models.libraries.Library;
import org.openfinna.java.connector.classes.models.loans.Loan;

import java.util.List;

public class HomepageSavedResources {

    @SerializedName("pickupBooks")
    private List<Hold> pickupBooks;
    @SerializedName("reservedBooks")
    private List<Loan> reservedBooks;
    @SerializedName("selectedLibrary")
    private Library selectedLibrary;
    @SerializedName("librarySelected")
    private boolean librarySelected;

    @SerializedName("libraryOptOut")
    private boolean libraryOptOut;

    public HomepageSavedResources(List<Hold> pickupBooks, List<Loan> reservedBooks, Library selectedLibrary, boolean librarySelected, boolean libraryOptOut) {
        this.pickupBooks = pickupBooks;
        this.reservedBooks = reservedBooks;
        this.selectedLibrary = selectedLibrary;
        this.librarySelected = librarySelected;
        this.libraryOptOut = libraryOptOut;
    }

    public static HomepageSavedResources getDefault() {
        return new HomepageSavedResources(null, null, null, false, false);
    }

    public List<Hold> getPickupBooks() {
        return pickupBooks;
    }

    public void setPickupBooks(List<Hold> pickupBooks) {
        this.pickupBooks = pickupBooks;
    }

    public List<Loan> getReservedBooks() {
        return reservedBooks;
    }

    public void setReservedBooks(List<Loan> reservedBooks) {
        this.reservedBooks = reservedBooks;
    }

    public Library getSelectedLibrary() {
        return selectedLibrary;
    }

    public void setSelectedLibrary(Library selectedLibrary) {
        this.selectedLibrary = selectedLibrary;
    }

    public boolean isLibrarySelected() {
        return librarySelected;
    }

    public void setLibrarySelected(boolean librarySelected) {
        this.librarySelected = librarySelected;
    }

    public boolean isLibraryOptOut() {
        return libraryOptOut;
    }

    public void setLibraryOptOut(boolean libraryOptOut) {
        this.libraryOptOut = libraryOptOut;
    }
}
