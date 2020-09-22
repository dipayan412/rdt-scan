package edu.washington.cs.ubicomplab.rdt_reader.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import edu.washington.cs.ubicomplab.rdt_reader.model.SampleID;

@Dao
public interface sampleIDDao {
    @Insert
    void insert(SampleID... sampleIDS);

    @Update
    void update(SampleID... sampleIDS);

    @Delete
    void delete(SampleID... sampleIDS);

    @Query("SELECT * FROM SampleID WHERE ID = :sid")
    SampleID getSampleWithID(String sid);
}

