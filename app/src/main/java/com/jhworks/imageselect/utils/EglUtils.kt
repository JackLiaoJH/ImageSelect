package com.jhworks.imageselect.utils

import android.annotation.TargetApi
import android.opengl.EGL14
import android.opengl.GLES10
import android.opengl.GLES20
import android.os.Build
import android.util.Log
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 17:40
 */
object EglUtils {
    private const val TAG = "EglUtils"
    fun getMaxTextureSize(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getMaxTextureEgl14()
            } else {
                getMaxTextureEgl10()
            }
        } catch (e: Exception) {
            Log.d(TAG, "getMaxTextureSize: ", e)
            0
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun getMaxTextureEgl14(): Int {
        val dpy = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val vers = IntArray(2)
        EGL14.eglInitialize(dpy, vers, 0, vers, 1)
        val configAttr = intArrayOf(
                EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
                EGL14.EGL_LEVEL, 0,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<android.opengl.EGLConfig>(1)
        val numConfig = IntArray(1)
        EGL14.eglChooseConfig(dpy, configAttr, 0,
                configs, 0, 1, numConfig, 0)
        if (numConfig[0] == 0) {
            return 0
        }
        val config = configs[0]
        val surfAttr = intArrayOf(EGL14.EGL_WIDTH, 64, EGL14.EGL_HEIGHT, 64, EGL14.EGL_NONE)
        val surf = EGL14.eglCreatePbufferSurface(dpy, config, surfAttr, 0)
        val ctxAttrib = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE
        )
        val ctx = EGL14.eglCreateContext(dpy, config, EGL14.EGL_NO_CONTEXT, ctxAttrib, 0)
        EGL14.eglMakeCurrent(dpy, surf, surf, ctx)
        val maxSize = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0)
        EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT)
        EGL14.eglDestroySurface(dpy, surf)
        EGL14.eglDestroyContext(dpy, ctx)
        EGL14.eglTerminate(dpy)
        return maxSize[0]
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private fun getMaxTextureEgl10(): Int {
        val egl = EGLContext.getEGL() as EGL10
        val dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        val vers = IntArray(2)
        egl.eglInitialize(dpy, vers)
        val configAttr = intArrayOf(
                EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                EGL10.EGL_LEVEL, 0,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
                EGL10.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfig = IntArray(1)
        egl.eglChooseConfig(dpy, configAttr, configs, 1, numConfig)
        if (numConfig[0] == 0) {
            return 0
        }
        val config = configs[0]
        val surfAttr = intArrayOf(
                EGL10.EGL_WIDTH, 64,
                EGL10.EGL_HEIGHT, 64,
                EGL10.EGL_NONE
        )
        val surf = egl.eglCreatePbufferSurface(dpy, config, surfAttr)
        val EGL_CONTEXT_CLIENT_VERSION = 0x3098 // missing in EGL10
        val ctxAttrib = intArrayOf(
                EGL_CONTEXT_CLIENT_VERSION, 1,
                EGL10.EGL_NONE
        )
        val ctx = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, ctxAttrib)
        egl.eglMakeCurrent(dpy, surf, surf, ctx)
        val maxSize = IntArray(1)
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0)
        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT)
        egl.eglDestroySurface(dpy, surf)
        egl.eglDestroyContext(dpy, ctx)
        egl.eglTerminate(dpy)
        return maxSize[0]
    }
}