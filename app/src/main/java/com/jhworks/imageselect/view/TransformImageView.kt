package com.jhworks.imageselect.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.IntRange
import androidx.appcompat.widget.AppCompatImageView
import com.jhworks.imageselect.utils.RectUtils
import com.jhworks.imageselect.crop.callback.BitmapLoadCallback
import com.jhworks.imageselect.crop.vo.ExifInfo
import com.jhworks.imageselect.utils.BitmapLoadUtils
import com.jhworks.imageselect.utils.FastBitmapDrawable
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 16:55
 */
open class TransformImageView(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
    : AppCompatImageView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)


    companion object {
        private val TAG = TransformImageView::class.java.simpleName

        private const val RECT_CORNER_POINTS_COORDS = 8
        private const val RECT_CENTER_POINT_COORDS = 2
        private const val MATRIX_VALUES_COUNT = 9
    }

    protected val mCurrentImageCorners = FloatArray(RECT_CORNER_POINTS_COORDS)
    protected val mCurrentImageCenter = FloatArray(RECT_CENTER_POINT_COORDS)

    private val mMatrixValues = FloatArray(MATRIX_VALUES_COUNT)

    protected var mCurrentImageMatrix = Matrix()
    protected var mThisWidth = 0
    protected var mThisHeight = 0

    protected var mTransformImageListener: TransformImageListener? = null

    private var mInitialImageCorners: FloatArray? = null
    private var mInitialImageCenter: FloatArray? = null

    protected var mBitmapDecoded = false
    protected var mBitmapLaidOut = false

    private var mMaxBitmapSize = 0

    private var mImageInputPath: String? = null
    private var mImageOutputPath: String? = null
    private var mExifInfo: ExifInfo? = null

    init {
        initView()
    }

    protected open fun initView() {
        scaleType = ScaleType.MATRIX
    }

    fun setTransformImageListener(transformImageListener: TransformImageListener) {
        mTransformImageListener = transformImageListener
    }

    /**
     * Setter for [.mMaxBitmapSize] value.
     * Be sure to call it before [.setImageURI] or other image setters.
     *
     * @param maxBitmapSize - max size for both width and height of bitmap that will be used in the view.
     */
    fun setMaxBitmapSize(maxBitmapSize: Int) {
        mMaxBitmapSize = maxBitmapSize
    }

    private fun getMaxBitmapSize(): Int {
        if (mMaxBitmapSize <= 0) {
            mMaxBitmapSize = BitmapLoadUtils.calculateMaxBitmapSize(context)
        }
        return mMaxBitmapSize
    }

    fun getImageInputPath(): String? {
        return mImageInputPath
    }

    fun getImageOutputPath(): String? {
        return mImageOutputPath
    }

    fun getExifInfo(): ExifInfo? {
        return mExifInfo
    }

    override fun setScaleType(scaleType: ScaleType?) {
        if (scaleType == ScaleType.MATRIX) {
            super.setScaleType(scaleType)
        } else {
            Log.w(TAG, "Invalid ScaleType. Only ScaleType.MATRIX can be used")
        }
    }

    override fun setImageBitmap(bm: Bitmap) {
        setImageDrawable(FastBitmapDrawable(bm))
    }


    /**
     * This method takes an Uri as a parameter, then calls method to decode it into Bitmap with specified size.
     *
     * @param imageUri - image Uri
     * @throws Exception - can throw exception if having problems with decoding Uri or OOM.
     */
    @Throws(Exception::class)
    fun setImageUri(imageUri: Uri, outputUri: Uri) {
        val maxBitmapSize = getMaxBitmapSize()
        BitmapLoadUtils.decodeBitmapInBackground(context, imageUri, outputUri, maxBitmapSize, maxBitmapSize,
                object : BitmapLoadCallback {
                    override fun onBitmapLoaded(bitmap: Bitmap, exifInfo: ExifInfo, imageInputPath: String, imageOutputPath: String) {
                        mImageInputPath = imageInputPath
                        mImageOutputPath = imageOutputPath
                        mExifInfo = exifInfo
                        mBitmapDecoded = true
                        setImageBitmap(bitmap)
                    }

                    override fun onFailure(bitmapWorkerException: Exception) {
                        Log.e(TAG, "onFailure: setImageUri", bitmapWorkerException)
                        mTransformImageListener?.onLoadFailure(bitmapWorkerException)
                    }
                })
    }

    /**
     * @return - current image scale value.
     * [1.0f - for original image, 2.0f - for 200% scaled image, etc.]
     */
    fun getCurrentScale(): Float {
        return getMatrixScale(mCurrentImageMatrix)
    }

    /**
     * This method calculates scale value for given Matrix object.
     */
    fun getMatrixScale(matrix: Matrix): Float {
        return sqrt(getMatrixValue(matrix, Matrix.MSCALE_X).toDouble().pow(2.0)
                + getMatrixValue(matrix, Matrix.MSKEW_Y).toDouble().pow(2.0)).toFloat()
    }

    /**
     * @return - current image rotation angle.
     */
    fun getCurrentAngle(): Float {
        return getMatrixAngle(mCurrentImageMatrix)
    }

    /**
     * This method calculates rotation angle for given Matrix object.
     */
    private fun getMatrixAngle(matrix: Matrix): Float {
        return (-(atan2(getMatrixValue(matrix, Matrix.MSKEW_X).toDouble(),
                getMatrixValue(matrix, Matrix.MSCALE_X).toDouble()) * (180 / Math.PI))).toFloat()
    }

    override fun setImageMatrix(matrix: Matrix?) {
        super.setImageMatrix(matrix)
        mCurrentImageMatrix.set(matrix)
        updateCurrentImagePoints()
    }

    fun getViewBitmap(): Bitmap? {
        return if (drawable == null || drawable !is FastBitmapDrawable) {
            null
        } else {
            (drawable as FastBitmapDrawable).getBitmap()
        }
    }


    /**
     * This method translates current image.
     *
     * @param deltaX - horizontal shift
     * @param deltaY - vertical shift
     */
    fun postTranslate(deltaX: Float, deltaY: Float) {
        if (deltaX != 0f || deltaY != 0f) {
            mCurrentImageMatrix.postTranslate(deltaX, deltaY)
            imageMatrix = mCurrentImageMatrix
        }
    }

    /**
     * This method scales current image.
     *
     * @param deltaScale - scale value
     * @param px         - scale center X
     * @param py         - scale center Y
     */
    open fun postScale(deltaScale: Float, px: Float, py: Float) {
        if (deltaScale != 0f) {
            mCurrentImageMatrix.postScale(deltaScale, deltaScale, px, py)
            imageMatrix = mCurrentImageMatrix
            mTransformImageListener?.onScale(getMatrixScale(mCurrentImageMatrix))
        }
    }

    /**
     * This method rotates current image.
     *
     * @param deltaAngle - rotation angle
     * @param px         - rotation center X
     * @param py         - rotation center Y
     */
    fun postRotate(deltaAngle: Float, px: Float, py: Float) {
        if (deltaAngle != 0f) {
            mCurrentImageMatrix.postRotate(deltaAngle, px, py)
            imageMatrix = mCurrentImageMatrix
            mTransformImageListener?.onRotate(getMatrixAngle(mCurrentImageMatrix))
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed || mBitmapDecoded && !mBitmapLaidOut) {
            val tempRight = width - paddingRight
            val tempBottom = height - paddingBottom
            mThisWidth = tempRight - paddingLeft
            mThisHeight = tempBottom - paddingTop
            onImageLaidOut()
        }
    }

    /**
     * When image is laid out [.mInitialImageCenter] and [.mInitialImageCenter]
     * must be set.
     */
    protected open fun onImageLaidOut() {
        drawable ?: return

        val w = drawable.intrinsicWidth.toFloat()
        val h = drawable.intrinsicHeight.toFloat()
        Log.d(TAG, String.format("Image size: [%d:%d]", w.toInt(), h.toInt()))

        val initialImageRect = RectF(0.0f, 0.0f, w, h)
        mInitialImageCorners = RectUtils.getCornersFromRect(initialImageRect)
        mInitialImageCenter = RectUtils.getCenterFromRect(initialImageRect)
        mBitmapLaidOut = true
        mTransformImageListener?.onLoadComplete()
    }

    /**
     * This method returns Matrix value for given index.
     *
     * @param matrix     - valid Matrix object
     * @param valueIndex - index of needed value. See [Matrix.MSCALE_X] and others.
     * @return - matrix value for index
     */
    protected fun getMatrixValue(matrix: Matrix, @IntRange(from = 0, to = MATRIX_VALUES_COUNT.toLong()) valueIndex: Int): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[valueIndex]
    }

    /**
     * This method logs given matrix X, Y, scale, and angle values.
     * Can be used for debug.
     */
    protected fun printMatrix(logPrefix: String, matrix: Matrix) {
        val x = getMatrixValue(matrix, Matrix.MTRANS_X)
        val y = getMatrixValue(matrix, Matrix.MTRANS_Y)
        val rScale = getMatrixScale(matrix)
        val rAngle = getMatrixAngle(matrix)
        Log.d(TAG, "$logPrefix: matrix: { x: $x, y: $y, scale: $rScale, angle: $rAngle }")
    }

    /**
     * This method updates current image corners and center points that are stored in
     * [.mCurrentImageCorners] and [.mCurrentImageCenter] arrays.
     * Those are used for several calculations.
     */
    private fun updateCurrentImagePoints() {
        mCurrentImageMatrix.mapPoints(mCurrentImageCorners, mInitialImageCorners)
        mCurrentImageMatrix.mapPoints(mCurrentImageCenter, mInitialImageCenter)
    }

    interface TransformImageListener {
        fun onLoadComplete()
        fun onLoadFailure(e: Exception)
        fun onRotate(currentAngle: Float)
        fun onScale(currentScale: Float)
    }
}