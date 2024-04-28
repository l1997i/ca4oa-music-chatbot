package com.luisli.cagpt.ui.activities
import android.os.Bundle
import com.luisli.cagpt.R
import com.luisli.cagpt.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}