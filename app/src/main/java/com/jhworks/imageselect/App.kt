package com.jhworks.imageselect

import android.app.Application
import android.os.StrictMode
import com.github.moduth.blockcanary.BlockCanary
import com.github.moduth.blockcanary.BlockCanaryContext
import com.jhworks.imageselect.engine.GlideEngine
import com.jhworks.library.ImageSelector

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 15:36
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        ImageSelector.setImageEngine(GlideEngine())

        if (BuildConfig.DEBUG) {
            enabledStrictMode()
            // 主进程
            BlockCanary.install(this, BlockCanaryContext()).start()
        }
    }

    private fun enabledStrictMode() {
        StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
//                .detectAll()
//                .detectDiskReads()
//                .detectDiskWrites()
                        .detectNetwork()
                        .penaltyLog()
                        .penaltyDialog()
                        .penaltyDeath()
                        .build()
        )

        StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                        //检测资源是否正确关闭
                        .detectLeakedClosableObjects()
                        .penaltyLog()
                        .build()
        );
    }
}