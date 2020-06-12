package com.jhworks.library.core.ui

import android.content.pm.PackageManager
import android.os.Build

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/6/11 15:55
 */
abstract class ImagePermissionActivity : ImageBaseActivity() {
    companion object {
        private const val REQUEST_PERMISSION_CODE = 10000
    }

    private val needRequestPermissionList = arrayListOf<String>()

    /**
     * 请求权限
     * @param permissions 权限列表,如{@link Manifest.permission.WRITE_EXTERNAL_STORAGE}
     */
    fun requestPermission(vararg permissions: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.forEach {
                if (checkSelfPermission(it) == PackageManager.PERMISSION_DENIED
                        && !needRequestPermissionList.contains(it)) {
                    needRequestPermissionList.add(it)
                }
            }
            if (needRequestPermissionList.size == 0) {
                onRequestPermissionSuccess()
                return
            }
            requestPermissions(
                    needRequestPermissionList.toArray(
                            arrayOfNulls(needRequestPermissionList.size)
                    ), REQUEST_PERMISSION_CODE
            )
            return
        }
        onRequestPermissionSuccess()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED
                        && needRequestPermissionList.contains(permissions[i])) {
                    needRequestPermissionList.remove(permissions[i])
                }
            }
            if (needRequestPermissionList.size == 0) {
                onRequestPermissionSuccess()
                return
            }
            onRequestPermissionFail(needRequestPermissionList)
        }
    }

    /**
     * 请求权限成功回调,只有所有的都成功才会回调
     */
    protected abstract fun onRequestPermissionSuccess()

    /**
     * 请求权限失败回调,只要有一个不成功都会回调该方法
     * @param deniedPermissions 失败的权限列表
     */
    protected abstract fun onRequestPermissionFail(deniedPermissions: MutableList<String>)
}