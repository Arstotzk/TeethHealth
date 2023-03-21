package com.example.TeethHealth

import android.graphics.Bitmap
import android.util.Log
import android.widget.TextView
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class FunctionsImage {
    fun ConvertToGray(compressImage: Bitmap): Bitmap? {
        Log.d("CV", "Before converting to gray")
        val imageMat = Mat()
        Utils.bitmapToMat(compressImage, imageMat)
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2RGB)
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2GRAY)
        Imgproc.GaussianBlur(imageMat, imageMat, Size(3.0, 3.0), 0.0)
        Imgproc.threshold(imageMat, imageMat, 0.0, 255.0, Imgproc.THRESH_OTSU)
        var newBitmap: Bitmap? = null
        newBitmap = compressImage.copy(compressImage.config, true)
        Utils.matToBitmap(imageMat, newBitmap)
        return newBitmap
    }

    companion object {
        fun UseMaskAndGetRidOfNoise(bitmapMain: Bitmap?, bitmapMask: Bitmap?): Bitmap? {
            var bitmapMain = bitmapMain
            val matMain = Mat()
            val matMask = Mat()
            Utils.bitmapToMat(bitmapMask, matMask)
            Utils.bitmapToMat(bitmapMain, matMain)
            Core.bitwise_and(matMain, matMask, matMain)
            Utils.matToBitmap(matMain, bitmapMain)
            bitmapMain = Erosion(bitmapMain, 3)
            bitmapMain = Dilatation(bitmapMain, 4)
            return bitmapMain
        }

        fun HSVMask(compressImage: Bitmap?, hMi: Int, sMi: Int, vMi: Int, hMa: Int, sMa: Int, vMa: Int): Bitmap? {
            Log.d("CV", "ContursTeeth")
            val imageMat = Mat()
            Utils.bitmapToMat(compressImage, imageMat)
            Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2HSV)
            val teethLow = Scalar(hMi.toDouble(), sMi.toDouble(), vMi.toDouble())
            val teethHigh = Scalar(hMa.toDouble(), sMa.toDouble(), vMa.toDouble())
            val thresh = Mat()
            Core.inRange(imageMat, teethLow, teethHigh, thresh)
            var newBitmap: Bitmap? = null
            newBitmap = compressImage!!.copy(compressImage.config, true)
            Utils.matToBitmap(thresh, newBitmap)
            return newBitmap
        }

        fun Dilatation(compressImage: Bitmap?, kernelSize: Int): Bitmap? {
            Log.d("CV", "Dilatation")
            val imageMat = Mat()
            Utils.bitmapToMat(compressImage, imageMat)
            val element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, Size((2 * kernelSize + 1).toDouble(), (2 * kernelSize + 1).toDouble()),
                    Point(kernelSize.toDouble(), kernelSize.toDouble()))
            Imgproc.dilate(imageMat, imageMat, element)
            val thresh = Mat()
            var newBitmap: Bitmap? = null
            newBitmap = compressImage!!.copy(compressImage.config, true)
            Utils.matToBitmap(imageMat, newBitmap)
            return newBitmap
        }

        fun Erosion(compressImage: Bitmap?, kernelSize: Int): Bitmap? {
            Log.d("CV", "Erosion")
            val imageMat = Mat()
            Utils.bitmapToMat(compressImage, imageMat)
            val element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, Size((2 * kernelSize + 1).toDouble(), (2 * kernelSize + 1).toDouble()),
                    Point(kernelSize.toDouble(), kernelSize.toDouble()))
            Imgproc.erode(imageMat, imageMat, element)
            val thresh = Mat()
            var newBitmap: Bitmap? = null
            newBitmap = compressImage!!.copy(compressImage.config, true)
            Utils.matToBitmap(imageMat, newBitmap)
            return newBitmap
        }

        fun Teeth(compressImage: Bitmap?): Bitmap? {
            Log.d("CV", "Teeth")
            var newBitmap = compressImage!!.copy(compressImage.config, true)
            val imageMat = Mat()
            Utils.bitmapToMat(compressImage, imageMat)
            Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2HSV)
            val avange = Core.mean(imageMat)
            Log.d("Teeth", "Среднее HSV:$avange")
            newBitmap = HSVMask(newBitmap, 0, 0, 130, 112, (avange.`val`[1] + 38).toInt(), 255)
            newBitmap = Dilatation(newBitmap, 15)
            newBitmap = Erosion(newBitmap, 25)
            newBitmap = Dilatation(newBitmap, 10)
            newBitmap = Erosion(newBitmap, 5)
            return newBitmap
        }

        fun TeethColor(compressImage: Bitmap?): Bitmap? {
            Log.d("CV", "TeethColor")
            val matTeeth = Mat()
            val matMain = Mat()
            var bitmapTeeth = compressImage?.copy(compressImage.config, true)
            val bitmapMain = compressImage?.copy(compressImage.config, true)
            bitmapTeeth = FindContur(bitmapTeeth)
            Utils.bitmapToMat(bitmapTeeth, matTeeth)
            Utils.bitmapToMat(bitmapMain, matMain)
            Core.bitwise_and(matMain, matTeeth, matMain)
            Utils.matToBitmap(matMain, bitmapMain)
            return bitmapMain
        }

        fun TestFilter(compressImage: Bitmap?): Bitmap? {
            Log.d("CV", "Test")
            val matMain = Mat()
            val bitmapMain = compressImage?.copy(compressImage?.config, true)
            Utils.bitmapToMat(bitmapMain, matMain)
            val matNew = matMain.clone()
            val sizeMatMain = matMain.size()
            var i = 0
            while (i < sizeMatMain.height) {
                var j = 0
                while (j < sizeMatMain.width) {
                    val data = matMain[i, j]
                    data[0] = data[0]
                    data[1] = (data[1] * 1.0).toInt().toDouble()
                    data[2] = (data[2] * 1.5).toInt().toDouble()
                    matNew.put(i, j, *data)
                    j++
                }
                i++
            }
            Utils.matToBitmap(matNew, bitmapMain)
            return bitmapMain
        }

        fun FindContur(compressImage: Bitmap?): Bitmap {
            var compressImage = compressImage
            Log.d("CV", "Контуры")
            compressImage = Teeth(compressImage)
            val matMain = Mat()
            val bitmapMain = compressImage!!.copy(compressImage.config, true)
            Utils.bitmapToMat(bitmapMain, matMain)
            Imgproc.cvtColor(matMain, matMain, Imgproc.COLOR_BGR2GRAY)
            val contours: List<MatOfPoint> = ArrayList()
            val hierarchy = Mat()
            Imgproc.findContours(matMain, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
            val drawing = Mat.zeros(matMain.size(), CvType.CV_8UC3)
            Log.d("CV", "Иерархия:$hierarchy")
            for (i in contours.indices) {
                Imgproc.drawContours(drawing, contours, i, Scalar(255.0, 255.0, 255.0), Core.FILLED, Core.LINE_8, hierarchy, 10, Point())
            }
            Utils.matToBitmap(drawing, bitmapMain)
            return bitmapMain
        }

        fun Caries(_compressImage: Bitmap?, _tvStatus: TextView?): Diseases {
            Log.d("CV", "Кариес")
            val matMain = Mat()
            var bitmapMain = _compressImage?.copy(_compressImage.config, true)
            Utils.bitmapToMat(bitmapMain, matMain)
            Imgproc.cvtColor(matMain, matMain, Imgproc.COLOR_BGR2HSV)
            var avange = Core.mean(matMain)
            Log.d("caries", "Среднее HSV:$avange")
            bitmapMain = HSVMask(bitmapMain, 0, 80, 0, 255, 255, 135)
            val bitmapMask = FindContur(_compressImage)
            bitmapMain = UseMaskAndGetRidOfNoise(bitmapMain, bitmapMask)
            Utils.bitmapToMat(bitmapMain, matMain)
            avange = Core.mean(matMain)
            Log.d("caries", "Среднее HSV:$avange")
            if (avange.`val`[2] > 1.45) {
                _tvStatus?.text = "Кариес найден"
            } else {
                _tvStatus?.text = "Кариес не найден"
            }
            return Diseases(bitmapMain, _tvStatus)
        }

        fun Gingivitis(_compressImage: Bitmap?, _tvStatus: TextView?): Diseases {
            Log.d("CV", "Гингивит")
            var bitmapMain = _compressImage?.copy(_compressImage.config, true)
            val matMain = Mat()
            Utils.bitmapToMat(bitmapMain, matMain)
            var bitmapMask: Bitmap? = FindContur(_compressImage)
            bitmapMask = HSVMask(bitmapMask, 0, 0, 0, 255, 255, 200)
            Imgproc.cvtColor(matMain, matMain, Imgproc.COLOR_BGR2HSV)
            var avange = Core.mean(matMain)
            Log.d("Gingivitis", "Среднее HSV:$avange")
            bitmapMain = HSVMask(bitmapMain, 115, (120 + avange.`val`[1] * 0.5).toInt(), (avange.`val`[2] - 50).toInt(), 123, 255, 255)
            bitmapMain = UseMaskAndGetRidOfNoise(bitmapMain, bitmapMask)
            Utils.bitmapToMat(bitmapMain, matMain)
            avange = Core.mean(matMain)
            Log.d("Gingivitis", "Среднее HSV для проверки:$avange")
            if (avange.`val`[2] > 3) {
                _tvStatus?.text = "Гингивит найден"
            } else {
                _tvStatus?.text = "Гингивит не найден"
            }
            return Diseases(bitmapMain, _tvStatus)
        }

        fun Gingivitis2(_compressImage: Bitmap?, _tvStatus: TextView?): Diseases {
            Log.d("CV", "Гингивит2")
            var bitmapMain = _compressImage?.copy(_compressImage.config, true)
            val matMain = Mat()
            Utils.bitmapToMat(bitmapMain, matMain)
            val bitmapMask = HSVMask(bitmapMain, 115, 65, 150, 134, 255, 255)
            Imgproc.cvtColor(matMain, matMain, Imgproc.COLOR_BGR2HSV)
            var avange = Core.mean(matMain)
            Log.d("Gingivitis2", "Среднее HSV:$avange")
            bitmapMain = HSVMask(bitmapMain, 115, (120 + avange.`val`[1] * 0.5).toInt(), (avange.`val`[2] - 50).toInt(), 123, 255, 255)
            bitmapMain = UseMaskAndGetRidOfNoise(bitmapMain, bitmapMask)
            Utils.bitmapToMat(bitmapMain, matMain)
            avange = Core.mean(matMain)
            Log.d("Gingivitis2", "Среднее HSV для проверки:$avange")
            if (avange.`val`[2] > 3) {
                _tvStatus?.text = "Гингивит найден"
            } else {
                _tvStatus?.text = "Гингивит не найден"
            }
            return Diseases(bitmapMain, _tvStatus)
        }
    }
}