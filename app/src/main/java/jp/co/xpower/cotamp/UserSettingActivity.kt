package jp.co.xpower.cotamp

import android.content.Intent
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
import jp.co.xpower.cotamp.databinding.ActivityUserSettingListBinding

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


        // header部分の設定
        binding.header.tvSettingName.text = getString(R.string.setting)
        binding.header.imBack.setOnClickListener {
            finish()
        }
    }

    private fun createSettingList() : MutableList<MutableMap<String, Any>> {
        val menuList: MutableList<MutableMap<String, Any>> = mutableListOf()
        var menu = mutableMapOf<String, Any>("name" to getString(R.string.setting_profile), "image" to R.drawable.setting_user_profile)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to getString(R.string.setting_mail), "image" to R.drawable.setting_mail)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to getString(R.string.setting_data_transfer), "image" to R.drawable.setting_install)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to getString(R.string.setting_notification), "image" to R.drawable.setting_bell)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to getString(R.string.setting_announce), "image" to R.drawable.setting_announce)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to getString(R.string.setting_usage), "image" to R.drawable.setting_info)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to getString(R.string.setting_info), "image" to R.drawable.stamp_icon)
        menuList.add(menu)
        menu = mutableMapOf<String, Any>("name" to getString(R.string.setting_contact), "image" to R.drawable.setting_question)
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

            when(settingName){
                getString(R.string.setting_profile) -> {
                    val intent2UserSetting = Intent(this@UserSettingActivity, SettingProfileActivity::class.java)
                    startActivity(intent2UserSetting)
                }
                getString(R.string.setting_mail) -> {
                    val intent2UserSetting = Intent(this@UserSettingActivity, SettingMailActivity::class.java)
                    startActivity(intent2UserSetting)
                }
                getString(R.string.setting_data_transfer) -> {
                    val intent2UserSetting = Intent(this@UserSettingActivity, SettingTransferActivity::class.java)
                    startActivity(intent2UserSetting)
                }
                getString(R.string.setting_notification) -> {
                    val intent2UserSetting = Intent(this@UserSettingActivity, SettingNotificationActivity::class.java)
                    startActivity(intent2UserSetting)
                }
                getString(R.string.setting_info) -> {
                    val intent2UserSetting = Intent(this@UserSettingActivity, SettingInfoActivity::class.java)
                    startActivity(intent2UserSetting)
                }
                else -> {
                    Toast.makeText(this@UserSettingActivity, settingName, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}