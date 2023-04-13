package com.example.stw_b1

var stampList = mutableListOf<Stamp>()

var STAMP_ID_EXTRA = "stampExtra"

class Stamp (
    var cover:Int,
    var title: String,
    var description: String,
    var id: Int? = stampList.size
)

