package com.ImagePickerApp.app

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.memory.PoolConfig
import com.facebook.imagepipeline.memory.PoolFactory
import com.lynx.service.http.LynxHttpService
import com.lynx.service.image.LynxImageService
import com.lynx.service.log.LynxLogService
import com.lynx.tasm.LynxEnv
import com.lynx.tasm.service.LynxServiceCenter

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initLynx()
    }

    private fun initLynx() {

        // Fresco
        val factory = PoolFactory(PoolConfig.newBuilder().build())
        val config = ImagePipelineConfig.newBuilder(applicationContext)
            .setPoolFactory(factory)
            .build()

        Fresco.initialize(applicationContext, config)

        // Lynx services
        LynxServiceCenter.inst().registerService(LynxImageService.getInstance())
        LynxServiceCenter.inst().registerService(LynxLogService)
        LynxServiceCenter.inst().registerService(LynxHttpService)

        // Lynx env
        LynxEnv.inst().init(this, null, null, null)

        // Image Picker module
        LynxEnv.inst().registerModule(
            "ImagePickerModule",
            ImagePickerModule::class.java
        )
    }
}
