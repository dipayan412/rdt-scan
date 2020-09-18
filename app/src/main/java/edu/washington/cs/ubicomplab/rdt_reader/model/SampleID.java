package edu.washington.cs.ubicomplab.rdt_reader.model;


import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import android.arch.persistence.room.Entity;

@Entity(tableName = "SampleID")
public class SampleID {
    @PrimaryKey
    @NonNull
    private String ID;

    @NonNull
    public String getID() {
        return ID;
    }

    public void setID(@NonNull String ID) {
        this.ID = ID;
    }
}

