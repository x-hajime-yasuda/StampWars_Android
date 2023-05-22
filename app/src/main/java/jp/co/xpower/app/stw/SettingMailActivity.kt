package jp.co.xpower.app.stw

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.co.xpower.app.stw.databinding.ActivitySettingMailBinding
import jp.co.xpower.app.stw.databinding.ActivitySettingUserProfileBinding

class SettingMailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingMailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_mail)

        binding = ActivitySettingMailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // header部分の設定
        binding.header.tvSettingName.text = getString(R.string.setting_mail)
        binding.header.imBack.setOnClickListener {
            finish()
        }
    }
}