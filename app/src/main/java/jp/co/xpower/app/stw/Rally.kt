package jp.co.xpower.app.stw

var rallyList = mutableListOf<Rally>()

var RALLY_ID_EXTRA = "rallyExtra"

class Rally (
    var cnId:String,
    var srId:String,
    var place:String?,
    var title: String?,
    var detail:String?,
    var rewardTitle:String?,
    var rewardDetail:String?,
    var cover:Int,
    var joined: Boolean,
    var selected: Boolean
)
