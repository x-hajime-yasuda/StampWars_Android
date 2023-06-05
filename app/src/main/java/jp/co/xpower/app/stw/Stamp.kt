package jp.co.xpower.app.stw

var stampList = mutableListOf<Stamp>()

var STAMP_ID_EXTRA = "stampExtra"

class Stamp (
    var cover:Int,
    var title: String?,
    var id: Int? = stampList.size
)
