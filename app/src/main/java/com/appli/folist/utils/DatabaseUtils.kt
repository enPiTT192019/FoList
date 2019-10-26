package com.appli.folist.utils

import io.realm.Realm

fun Realm.executeTransactionIfNotInTransaction(function:(Realm)->Unit){
    val inTransaction=this.isInTransaction
    if(!inTransaction)this.beginTransaction()
    function(this)
    if(!inTransaction)this.commitTransaction()
}