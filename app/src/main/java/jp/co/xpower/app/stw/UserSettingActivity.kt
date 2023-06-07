package jp.co.xpower.app.stw

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UserSettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setting_list)

        val rvSettingList = findViewById<RecyclerView>(R.id.rvSettingList)
        val layout = LinearLayoutManager(this@UserSettingActivity)
        rvSettingList.layoutManager = layout
        val settingList = createSettingList()
        val adapter = RecyclerListAdopter(settingList)
        rvSettingList.adapter = adapter
    }

    private fun createSettingList() : MutableList<MutableMap<String, Any>> {
        val menuList: MutableList<MutableMap<String, Any>> = mutableListOf()
        val menu = mutableMapOf<String, Any>("name" to "プロフィール", "image" to R.drawable.no_image)
        menuList.add(menu)

        return menuList
    }

    private inner class RecyclerListViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        var _settingName : TextView
        var _image : ImageView

        init {
            _settingName = itemView.findViewById(R.id.settingName)
            _image = itemView.findViewById(R.id.settingImage)
        }
    }

    private inner class RecyclerListAdopter(private val _listData : MutableList<MutableMap<String, Any>>) : RecyclerView.Adapter<RecyclerListViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerListViewHolder {
            val inflater = LayoutInflater.from(this@UserSettingActivity)
            val view = inflater.inflate(R.layout.row_user_setting, parent, false)
            val holder = RecyclerListViewHolder(view)
            return holder
        }

        override fun onBindViewHolder(holder: RecyclerListViewHolder, position: Int) {
            val item = _listData[position]
            val settingName = item["name"] as String
            val settingImage = item["image"] as Int

            holder._settingName.setText(settingName)
            holder._image.setImageResource(settingImage)
        }

        override fun getItemCount(): Int {
            return _listData.size
        }
    }
}