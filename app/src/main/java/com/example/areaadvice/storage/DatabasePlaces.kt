package com.example.areaadvice.storage

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.RowId

class DatabasePlaces(context: Context): SQLiteOpenHelper(context, name,null, version) {
    private val entries = "CREATE TABLE IF NOT EXISTS $Table_Name (" +
            "$Col_Id INTEGER PRIMARY KEY AUTOINCREMENT, $Col_place_Name TEXT, $Col_Address TEXT, " +
            "$Col_Rating TEXT, $Col_Lat TEXT, $Col_Lng TEXT, $Col_Schedule TEXT, $Col_Open TEXT)"
    private val del = "Drop Table If Exists $Table_Name"

    override fun onCreate(db: SQLiteDatabase){
        db.execSQL(entries)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(del)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun getAllRows(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $Table_Name", null)
    }

    fun deleteRow( id: Int){
        writableDatabase.delete(Table_Name, "$Col_Id=$id",null)>0
    }

    companion object {
        const val name = "Saved_Places.db"
        const val version = 1
        const val Table_Name = "Saved_Places"
        const val Col_place_Name = "Place_Name"
        const val Col_Lat = "Latitude"
        const val Col_Lng = "Longitude"
        const val Col_Rating = "Rating"
        const val Col_Address = "Address"
        const val Col_Schedule = "Schedule"
        const val Col_Open = "Open"
        const val Col_Id = "Id"
    }
}
