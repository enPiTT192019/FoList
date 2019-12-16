package com.appli.folist.models

data class TestScore(var count:Int,var correct:Int){
    fun getProgress():Double{
        return correct.toDouble()/count
    }
}