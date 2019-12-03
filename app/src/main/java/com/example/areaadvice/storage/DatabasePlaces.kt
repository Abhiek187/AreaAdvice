package com.example.areaadvice.storage

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabasePlaces(context: Context): SQLiteOpenHelper(context,
    name,null,
    version
){

    private val entries ="Create Table $Table_Name "+
            "($Id INTEGER PRIMARY KEY, $Col_place_Name Text, $Col_Address Text, $Col_Rating Text, $Col_Lat Text, $Col_Lng Text,$Col_Schedule Text, $Col_Open Text)"
    private val del="Drop Table If Exists $Table_Name"
    override fun onCreate(db: SQLiteDatabase){
        db.execSQL(entries)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(del)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db,oldVersion,newVersion)
    }

    fun getAllRows(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $Table_Name", null)
    }

    companion object{
        const val name="Saved_Places.db"
        const val version =1
        const val Table_Name="Saved_Places"
        const val Col_place_Name="Place_Name"
        const val Col_Lat="Latitude"
        const val Col_Lng="Longitude"
        const val Col_Rating="Rating"
        const val Col_Address="Address"
        const val Col_Schedule="Schedule"
        const val Col_Open="Open"
        const val Id="Id"
    }
}