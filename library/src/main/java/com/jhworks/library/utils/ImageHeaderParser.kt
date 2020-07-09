package com.jhworks.library.utils

import android.text.TextUtils
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2020/7/7 18:15
 */
class ImageHeaderParser(inputStream: InputStream) {

    companion object {
        private const val TAG = "ImageHeaderParser"

        /**
         * A constant indicating we were unable to parse the orientation from the image either because
         * no exif segment containing orientation data existed, or because of an I/O error attempting to
         * read the exif segment.
         */
        const val UNKNOWN_ORIENTATION = -1

        private const val EXIF_MAGIC_NUMBER = 0xFFD8

        // "MM".
        private const val MOTOROLA_TIFF_MAGIC_NUMBER = 0x4D4D

        // "II".
        private const val INTEL_TIFF_MAGIC_NUMBER = 0x4949
        private const val JPEG_EXIF_SEGMENT_PREAMBLE = "Exif\u0000\u0000"
        private val JPEG_EXIF_SEGMENT_PREAMBLE_BYTES = JPEG_EXIF_SEGMENT_PREAMBLE.toByteArray(Charset.forName("UTF-8"))
        private const val SEGMENT_SOS = 0xDA
        private const val MARKER_EOI = 0xD9
        private const val SEGMENT_START_ID = 0xFF
        private const val EXIF_SEGMENT_TYPE = 0xE1
        private const val ORIENTATION_TAG_TYPE = 0x0112
        private val BYTES_PER_FORMAT = intArrayOf(0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8)


        fun copyExif(originalExif: ExifInterface, width: Int, height: Int, imageOutputPath: String) {
            val attributes = arrayOf(
                    ExifInterface.TAG_F_NUMBER,
                    ExifInterface.TAG_DATETIME,
                    ExifInterface.TAG_DATETIME_DIGITIZED,
                    ExifInterface.TAG_EXPOSURE_TIME,
                    ExifInterface.TAG_FLASH,
                    ExifInterface.TAG_FOCAL_LENGTH,
                    ExifInterface.TAG_GPS_ALTITUDE,
                    ExifInterface.TAG_GPS_ALTITUDE_REF,
                    ExifInterface.TAG_GPS_DATESTAMP,
                    ExifInterface.TAG_GPS_LATITUDE,
                    ExifInterface.TAG_GPS_LATITUDE_REF,
                    ExifInterface.TAG_GPS_LONGITUDE,
                    ExifInterface.TAG_GPS_LONGITUDE_REF,
                    ExifInterface.TAG_GPS_PROCESSING_METHOD,
                    ExifInterface.TAG_GPS_TIMESTAMP,
                    ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
                    ExifInterface.TAG_MAKE,
                    ExifInterface.TAG_MODEL,
                    ExifInterface.TAG_SUBSEC_TIME,
                    ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
                    ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
                    ExifInterface.TAG_WHITE_BALANCE
            )
            try {
                val newExif = ExifInterface(imageOutputPath)
                var value: String?
                for (attribute in attributes) {
                    value = originalExif.getAttribute(attribute)
                    if (!TextUtils.isEmpty(value)) {
                        newExif.setAttribute(attribute, value)
                    }
                }
                newExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, width.toString())
                newExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, height.toString())
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, "0")
                newExif.saveAttributes()
            } catch (e: IOException) {
                Log.d(TAG, "${e.message}")
            }
        }
    }

    private val reader: Reader = StreamReader(inputStream)


    /**
     * Parse the orientation from the image header. If it doesn't handle this image type (or this is
     * not an image) it will return a default value rather than throwing an exception.
     *
     * @return The exif orientation if present or -1 if the header couldn't be parsed or doesn't
     * contain an orientation
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getOrientation(): Int {
        val magicNumber = reader.uInt16
        return if (!handles(magicNumber)) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Parser doesn't handle magic number: $magicNumber")
            }
            UNKNOWN_ORIENTATION
        } else {
            val exifSegmentLength = moveToExifSegmentAndGetLength()
            if (exifSegmentLength == -1) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Failed to parse exif segment length, or exif segment not found")
                }
                return UNKNOWN_ORIENTATION
            }
            val exifData = ByteArray(exifSegmentLength)
            parseExifSegment(exifData, exifSegmentLength)
        }
    }

    @Throws(IOException::class)
    private fun parseExifSegment(tempArray: ByteArray, exifSegmentLength: Int): Int {
        val read = reader.read(tempArray, exifSegmentLength)
        if (read != exifSegmentLength) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Unable to read exif segment data"
                        + ", length: " + exifSegmentLength
                        + ", actually read: " + read)
            }
            return UNKNOWN_ORIENTATION
        }
        val hasJpegExifPreamble = hasJpegExifPreamble(tempArray, exifSegmentLength)
        return if (hasJpegExifPreamble) {
            parseExifSegment(RandomAccessReader(tempArray, exifSegmentLength))
        } else {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Missing jpeg exif preamble")
            }
            UNKNOWN_ORIENTATION
        }
    }

    private fun hasJpegExifPreamble(exifData: ByteArray?, exifSegmentLength: Int): Boolean {
        var result = exifData != null && exifSegmentLength > JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.size
        if (result) {
            for (i in JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.indices) {
                if (exifData!![i] != JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.get(i)) {
                    result = false
                    break
                }
            }
        }
        return result
    }

    /**
     * Moves reader to the start of the exif segment and returns the length of the exif segment or
     * `-1` if no exif segment is found.
     */
    @Throws(IOException::class)
    private fun moveToExifSegmentAndGetLength(): Int {
        var segmentId: Short
        var segmentType: Short
        var segmentLength: Int
        while (true) {
            segmentId = reader.uInt8
            if (segmentId.toInt() != SEGMENT_START_ID) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Unknown segmentId=$segmentId")
                }
                return -1
            }
            segmentType = reader.uInt8
            if (segmentType.toInt() == SEGMENT_SOS) {
                return -1
            } else if (segmentType.toInt() == MARKER_EOI) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Found MARKER_EOI in exif segment")
                }
                return -1
            }

            // Segment length includes bytes for segment length.
            segmentLength = reader.uInt16 - 2
            if (segmentType.toInt() != EXIF_SEGMENT_TYPE) {
                val skipped = reader.skip(segmentLength.toLong())
                if (skipped != segmentLength.toLong()) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "Unable to skip enough data"
                                + ", type: " + segmentType
                                + ", wanted to skip: " + segmentLength
                                + ", but actually skipped: " + skipped)
                    }
                    return -1
                }
            } else {
                return segmentLength
            }
        }
    }

    private fun parseExifSegment(segmentData: RandomAccessReader): Int {
        val headerOffsetSize: Int = JPEG_EXIF_SEGMENT_PREAMBLE.length
        val byteOrderIdentifier = segmentData.getInt16(headerOffsetSize)
        val byteOrder: ByteOrder
        byteOrder = when {
            byteOrderIdentifier.toInt() == MOTOROLA_TIFF_MAGIC_NUMBER -> {
                ByteOrder.BIG_ENDIAN
            }
            byteOrderIdentifier.toInt() == INTEL_TIFF_MAGIC_NUMBER -> {
                ByteOrder.LITTLE_ENDIAN
            }
            else -> {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Unknown endianness = $byteOrderIdentifier")
                }
                ByteOrder.BIG_ENDIAN
            }
        }
        segmentData.order(byteOrder)
        val firstIfdOffset = segmentData.getInt32(headerOffsetSize + 4) + headerOffsetSize
        val tagCount = segmentData.getInt16(firstIfdOffset).toInt()
        var tagOffset: Int
        var tagType: Int
        var formatCode: Int
        var componentCount: Int
        for (i in 0 until tagCount) {
            tagOffset = calcTagOffset(firstIfdOffset, i)
            tagType = segmentData.getInt16(tagOffset).toInt()

            // We only want orientation.
            if (tagType != ORIENTATION_TAG_TYPE) {
                continue
            }
            formatCode = segmentData.getInt16(tagOffset + 2).toInt()

            // 12 is max format code.
            if (formatCode < 1 || formatCode > 12) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Got invalid format code = $formatCode")
                }
                continue
            }
            componentCount = segmentData.getInt32(tagOffset + 4)
            if (componentCount < 0) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Negative tiff component count")
                }
                continue
            }
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Got tagIndex=" + i + " tagType=" + tagType + " formatCode=" + formatCode
                        + " componentCount=" + componentCount)
            }
            val byteCount: Int = componentCount + BYTES_PER_FORMAT.get(formatCode)
            if (byteCount > 4) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Got byte count > 4, not orientation, continuing, formatCode=$formatCode")
                }
                continue
            }
            val tagValueOffset = tagOffset + 8
            if (tagValueOffset < 0 || tagValueOffset > segmentData.length()) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Illegal tagValueOffset=$tagValueOffset tagType=$tagType")
                }
                continue
            }
            if (byteCount < 0 || tagValueOffset + byteCount > segmentData.length()) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Illegal number of bytes for TI tag data tagType=$tagType")
                }
                continue
            }

            //assume componentCount == 1 && fmtCode == 3
            return segmentData.getInt16(tagValueOffset).toInt()
        }
        return -1
    }

    private fun calcTagOffset(ifdOffset: Int, tagIndex: Int): Int {
        return ifdOffset + 2 + 12 * tagIndex
    }

    private fun handles(imageMagicNumber: Int): Boolean {
        return imageMagicNumber and EXIF_MAGIC_NUMBER == EXIF_MAGIC_NUMBER || imageMagicNumber == MOTOROLA_TIFF_MAGIC_NUMBER || imageMagicNumber == INTEL_TIFF_MAGIC_NUMBER
    }

    private class RandomAccessReader(data: ByteArray?, length: Int) {
        private val data: ByteBuffer
        fun order(byteOrder: ByteOrder?) {
            data.order(byteOrder)
        }

        fun length(): Int {
            return data.remaining()
        }

        fun getInt32(offset: Int): Int {
            return data.getInt(offset)
        }

        fun getInt16(offset: Int): Short {
            return data.getShort(offset)
        }

        init {
            this.data = ByteBuffer.wrap(data)
                    .order(ByteOrder.BIG_ENDIAN)
                    .limit(length) as ByteBuffer
        }
    }

    private interface Reader {
        @get:Throws(IOException::class)
        val uInt16: Int

        @get:Throws(IOException::class)
        val uInt8: Short

        @Throws(IOException::class)
        fun skip(total: Long): Long

        @Throws(IOException::class)
        fun read(buffer: ByteArray?, byteCount: Int): Int
    }

    private class StreamReader // Motorola / big endian byte order.
    (private val `is`: InputStream) : Reader {
        @get:Throws(IOException::class)
        override val uInt16: Int
            get() = `is`.read() shl 8 and 0xFF00 or (`is`.read() and 0xFF)

        @get:Throws(IOException::class)
        override val uInt8: Short
            get() = (`is`.read() and 0xFF).toShort()

        @Throws(IOException::class)
        override fun skip(total: Long): Long {
            if (total < 0) {
                return 0
            }
            var toSkip = total
            while (toSkip > 0) {
                val skipped = `is`.skip(toSkip)
                if (skipped > 0) {
                    toSkip -= skipped
                } else {
                    // Skip has no specific contract as to what happens when you reach the end of
                    // the stream. To differentiate between temporarily not having more data and
                    // having finished the stream, we read a single byte when we fail to skip any
                    // amount of data.
                    val testEofByte = `is`.read()
                    if (testEofByte == -1) {
                        break
                    } else {
                        toSkip--
                    }
                }
            }
            return total - toSkip
        }

        @Throws(IOException::class)
        override fun read(buffer: ByteArray?, byteCount: Int): Int {
            var toRead = byteCount
            var read = 0
            while (toRead > 0 && `is`.read(buffer, byteCount - toRead, toRead).also { read = it } != -1) {
                toRead -= read
            }
            return byteCount - toRead
        }

    }


}