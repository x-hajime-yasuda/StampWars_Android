package jp.co.xpower.cotamp

var stampList = mutableListOf<Stamp>()

var STAMP_ID_EXTRA = "stampExtra"

class Stamp (
    var cover:Int,
    var title: String?,
    var id: Int? = stampList.size
)
