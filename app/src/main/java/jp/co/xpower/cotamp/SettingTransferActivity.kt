package jp.co.xpower.cotamp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.co.xpower.cotamp.databinding.ActivitySettingTransferBinding

class SettingTransferActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingTransferBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_transfer)

        binding = ActivitySettingTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // header部分の設定
        binding.header.tvSettingName.text = getString(R.string.setting_data_transfer)
        binding.header.imBack.setOnClickListener {
            finish()
        }
    }
}