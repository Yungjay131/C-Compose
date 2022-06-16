package com.slyworks.models

data class CryptoModel(
    var id:String,
    val _id: Int,
    val image:String,
    val symbol:String,
    val name:String,
    val maxSupply:Double?,
    val circulatingSupply:Double?,
    val totalSupply:Double?,
    val cmcRank:Int,
    val lastUpdated:String,
    val price:Double,
    val priceUnit:String,
    val marketCap:Double?,
    val dateAdded: String,
    val tags:String,//this would be a , separated string
    val isFavorite:Boolean = false
)