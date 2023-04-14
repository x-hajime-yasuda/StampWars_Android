package jp.co.xpower.app.stw

var rallyList = mutableListOf<Rally>()

var RALLY_ID_EXTRA = "rallyExtra"

class Rally (
    var cover:Int,
    var title: String,
    var description: String,
    var id: Int? = rallyList.size
)
