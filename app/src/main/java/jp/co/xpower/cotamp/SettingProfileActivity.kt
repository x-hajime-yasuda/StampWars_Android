package jp.co.xpower.cotamp

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import jp.co.xpower.cotamp.databinding.ActivitySettingUserProfileBinding
import java.util.Calendar

class SettingProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingUserProfileBinding
    lateinit var datePickerDialog: DatePickerDialog

    private val _dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        val cal = Calendar.getInstance()
        cal.set(year, month, dayOfMonth)

        // monthは0～11で表されるため+1する
        binding.btnBirth.text = getString(R.string.setting_selected_birth, year, month+1, dayOfMonth)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_user_profile)

        binding = ActivitySettingUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //誕生日選択ボタンの設定
        binding.btnBirth.text = getString(R.string.setting_birth)
        binding.btnBirth.setOnClickListener {
            selectDate(it)
        }

        // header部分の設定
        binding.header.tvSettingName.text = getString(R.string.setting_profile)
        binding.header.imBack.setOnClickListener {
            finish()
        }
    }

    private fun selectDate(view : View){
        val calendar = Calendar.getInstance()
        val year : Int = calendar.get(Calendar.YEAR)
        val month : Int = calendar.get(Calendar.MONTH)
        val day : Int = calendar.get(Calendar.DAY_OF_MONTH)

        datePickerDialog = DatePickerDialog(this, _dateSetListener, year, month, day)

        datePickerDialog.show()
    }

}