package com.ahahahq.scandemo.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ahahahq.scandemo.common.utils.StatusBarUtil

abstract class BaseAppCompatActivity : AppCompatActivity() {
    protected var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.initStatusBar(this)
    }

    open fun initToolbar(toolbarTitle: String) {
        toolbar = findViewById(R.id.toolbar)
        toolbar?.apply {
            title = toolbarTitle
            this.setNavigationOnClickListener {
                onBackPressed()
            }
            setNavigationIcon(R.drawable.back)
        }
    }
}