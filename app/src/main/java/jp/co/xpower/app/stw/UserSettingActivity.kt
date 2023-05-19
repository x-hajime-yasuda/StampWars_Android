package jp.co.xpower.app.stw

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.xpower.app.stw.databinding.ActivityMainBinding
import jp.co.xpower.app.stw.databinding.ActivityUserSettingListBinding

class UserSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserSettingListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_setting_list)

        binding = ActivityUserSettingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // リサイクラービューの設定
        val rvSettingList = findViewById<RecyclerView>(R.id.rvSettingList)
        val layout = LinearLayoutManager(this@UserSettingActivity)
        rvSettingList.layoutManager = layout
        val settingList = createSettingList()
        val adapter = RecyclerListAdopter(settingList)
        rvSettingList.adapter = adapter

        binding.imBack.setOnClickListener {
            finish()
        }
    }

    private fun createSettingList() : MutableList<MutableMap<String, Any>> {
        val menuList: MutableList<MutableMap<String, Any>> = mutableListOf()

        var menu = mutableMapOf<String, Any>("name" to "プロフィール設定", "image" to R.drawable.setting_user_profile)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to "メールアドレス設定", "image" to R.drawable.setting_mail)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to "データ引き継ぎ", "image" to R.drawable.setting_install)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to "通知", "image" to R.drawable.setting_bell)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to "お知らせ", "image" to R.drawable.setting_announce)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to "アプリの使い方", "image" to R.drawable.setting_info)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to "お問い合わせ", "image" to R.drawable.setting_question)
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

            view.setOnClickListener(ItemClickListener())
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

    private inner class ItemClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            val tvSettingName = v.findViewById<TextView>(R.id.settingName)
            val settingName = tvSettingName.text.toString()
            Toast.makeText(this@UserSettingActivity, settingName, Toast.LENGTH_SHORT).show()
        }
    }
}