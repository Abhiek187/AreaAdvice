package com.example.areaadvice

import android.content.Context
import android.content.SharedPreferences

class Database (var context: Context)
{

    val pre: SharedPreferences = context.getSharedPreferences("MyPref", 0)

    //var pre = context.getSharedPreferences("MyPref", 0)
    var editor = pre.edit()
}