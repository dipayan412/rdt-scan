package edu.washington.cs.ubicomplab.rdt_reader.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import edu.washington.cs.ubicomplab.rdt_reader.model.SampleID;


@Database(entities = {SampleID.class},version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract sampleIDDao getSampleIDDao();
}
