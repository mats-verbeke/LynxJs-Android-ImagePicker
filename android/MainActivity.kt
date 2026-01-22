package com.ImagePickerApp.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.lynx.tasm.LynxView
import com.lynx.tasm.LynxViewBuilder
import com.lynx.xelement.XElementBehaviors

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val builder = LynxViewBuilder()
        builder.addBehaviors(XElementBehaviors().create())
        builder.setTemplateProvider(HttpTemplateProvider())

        val lynxView: LynxView = builder.build(this)
        setContentView(lynxView)

        lynxView.renderTemplateUrl(
            "http://YOUR_DEV_SERVER/main.lynx.bundle",
            ""
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePickerModule.onActivityResult(requestCode, resultCode, data)
    }
}
