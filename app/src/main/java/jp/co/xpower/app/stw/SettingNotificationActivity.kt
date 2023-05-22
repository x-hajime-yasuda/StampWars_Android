package jp.co.xpower.app.stw

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.co.xpower.app.stw.databinding.ActivitySettingNotificationBinding

class SettingNotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_notification)

        binding = ActivitySettingNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // header部分の設定
        binding.header.tvSettingName.text = getString(R.string.setting_notification)
        binding.header.imBack.setOnClickListener {
            finish()
        }
    }
}