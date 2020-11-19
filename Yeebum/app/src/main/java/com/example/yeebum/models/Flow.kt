package com.example.yeebum.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.inject.Inject


@Entity(tableName = "flows_table")
data class Flow @Inject constructor(@PrimaryKey(autoGenerate = true) val id:Int, val name:String, val duration:Int, val actions:String)
