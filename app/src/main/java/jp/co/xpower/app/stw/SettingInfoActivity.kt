package jp.co.xpower.app.stw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import jp.co.xpower.app.stw.databinding.ActivitySettingInfoBinding
import jp.co.xpower.app.stw.databinding.ActivitySettingMailBinding

class SettingInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_info)

        binding = ActivitySettingInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvLicense.setOnClickListener {
            startActivity(Intent(this@SettingInfoActivity, OssLicensesMenuActivity::class.java))
        }

        // header部分の設定
        binding.header.tvSettingName.text = getString(R.string.setting_info)
        binding.header.imBack.setOnClickListener {
            finish()
        }
    }
}