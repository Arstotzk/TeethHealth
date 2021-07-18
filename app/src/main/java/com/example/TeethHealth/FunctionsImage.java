package com.example.TeethHealth;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class FunctionsImage {

    public static Bitmap UseMaskAndGetRidOfNoise(Bitmap bitmapMain, Bitmap bitmapMask){
        Mat matMain = new Mat();
        Mat matMask = new Mat();
        Utils.bitmapToMat(bitmapMask, matMask);
        Utils.bitmapToMat(bitmapMain, matMain);
        Core.bitwise_and(matMain, matMask, matMain);
        Utils.matToBitmap(matMain, bitmapMain);
        bitmapMain = Erosion(bitmapMain, 3);
        bitmapMain = Dilatation(bitmapMain, 4);
        return bitmapMain;
    }
    public Bitmap ConvertToGray(Bitmap compressImage){
        Log.d("CV", "Before converting to gray");
        Mat imageMat = new Mat();
        Utils.bitmapToMat(compressImage, imageMat);
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(imageMat, imageMat, new Size(3, 3), 0);
        Imgproc.threshold(imageMat, imageMat, 0, 255, Imgproc.THRESH_OTSU);
        Bitmap newBitmap = null;
        newBitmap = compressImage.copy(compressImage.getConfig(), true);
        Utils.matToBitmap(imageMat, newBitmap);
        return newBitmap;
    }
    public static Bitmap HSVMask(Bitmap compressImage, int hMi, int sMi, int vMi, int hMa, int sMa, int vMa){
        Log.d("CV", "ContursTeeth");
        Mat imageMat = new Mat();
        Utils.bitmapToMat(compressImage, imageMat);
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2HSV);
        Scalar teethLow = new Scalar(hMi, sMi, vMi);
        Scalar teethHigh = new Scalar(hMa, sMa , vMa);
        Mat thresh = new Mat();
        Core.inRange(imageMat, teethLow, teethHigh, thresh);
        Bitmap newBitmap = null;
        newBitmap = compressImage.copy(compressImage.getConfig(), true);
        Utils.matToBitmap(thresh, newBitmap);
        return newBitmap;
    }
    public static Bitmap Dilatation(Bitmap compressImage, int kernelSize){
        Log.d("CV", "Dilatation");
        Mat imageMat = new Mat();
        Utils.bitmapToMat(compressImage, imageMat);
        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                new Point(kernelSize, kernelSize));
        Imgproc.dilate(imageMat, imageMat, element);
        Mat thresh = new Mat();
        Bitmap newBitmap = null;
        newBitmap = compressImage.copy(compressImage.getConfig(), true);
        Utils.matToBitmap(imageMat, newBitmap);
        return newBitmap;
    }

    public static Bitmap Erosion(Bitmap compressImage, int kernelSize){
        Log.d("CV", "Erosion");
        Mat imageMat = new Mat();
        Utils.bitmapToMat(compressImage, imageMat);
        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                new Point(kernelSize, kernelSize));
        Imgproc.erode(imageMat, imageMat, element);
        Mat thresh = new Mat();
        Bitmap newBitmap = null;
        newBitmap = compressImage.copy(compressImage.getConfig(), true);
        Utils.matToBitmap(imageMat, newBitmap);
        return newBitmap;
    }
    public static Bitmap Teeth(Bitmap compressImage){
        Log.d("CV", "Teeth");
        Bitmap newBitmap = compressImage.copy(compressImage.getConfig(), true);
        Mat imageMat = new Mat();
        Utils.bitmapToMat(compressImage, imageMat);
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2HSV);
        Scalar avange = Core.mean(imageMat);
        Log.d("Teeth", "Среднее HSV:" + avange);
        newBitmap = HSVMask(newBitmap, 0,0,130,112,(int) ((avange.val[1]) + 38),255);
        newBitmap = Dilatation(newBitmap, 15);
        newBitmap = Erosion(newBitmap, 25);
        newBitmap = Dilatation(newBitmap, 10);
        newBitmap = Erosion(newBitmap, 5);

        return newBitmap;
    }
    public static Bitmap TeethColor(Bitmap compressImage){
        Log.d("CV", "TeethColor");
        Mat matTeeth = new Mat();
        Mat matMain = new Mat();
        Bitmap bitmapTeeth = compressImage.copy(compressImage.getConfig(), true);
        Bitmap bitmapMain = compressImage.copy(compressImage.getConfig(), true);
        bitmapTeeth = FindContur(bitmapTeeth);
        Utils.bitmapToMat(bitmapTeeth, matTeeth);
        Utils.bitmapToMat(bitmapMain, matMain);
        Core.bitwise_and(matMain, matTeeth, matMain);
        Utils.matToBitmap(matMain, bitmapMain);
        return bitmapMain;
    }
    public static Bitmap TestFilter(Bitmap compressImage){
        Log.d("CV", "Test");
        Mat matMain = new Mat();
        Bitmap bitmapMain = compressImage.copy(compressImage.getConfig(), true);
        Utils.bitmapToMat(bitmapMain, matMain);
        Mat matNew = matMain.clone();
        Size sizeMatMain = matMain.size();
        for (int i = 0; i < sizeMatMain.height; i++)
            for (int j = 0; j < sizeMatMain.width; j++) {
                double[] data = matMain.get(i, j);
                data[0] = data[0] ;
                data[1] = (int)(data[1] * 1.0);
                data[2] = (int)(data[2] * 1.5);
                matNew.put(i, j, data);
            }
        Utils.matToBitmap(matNew, bitmapMain);
        return bitmapMain;
    }
    public static Bitmap FindContur(Bitmap compressImage){
        Log.d("CV", "Контуры");
        compressImage = Teeth(compressImage);
        Mat matMain = new Mat();
        Bitmap bitmapMain = compressImage.copy(compressImage.getConfig(), true);
        Utils.bitmapToMat(bitmapMain, matMain);
        Imgproc.cvtColor(matMain, matMain, Imgproc.COLOR_BGR2GRAY);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(matMain, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat drawing = Mat.zeros(matMain.size(), CvType.CV_8UC3);
        Log.d("CV", "Иерархия:" + hierarchy);
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(drawing, contours, i, new Scalar(255, 255, 255), Core.FILLED, Core.LINE_8, hierarchy, 10, new Point());
        }
        Utils.matToBitmap(drawing,bitmapMain);
        return bitmapMain;
    }
    public static Diseases Caries(Bitmap _compressImage, TextView _tvStatus){
        Log.d("CV", "Кариес");
        Mat matMain = new Mat();
        Bitmap bitmapMain = _compressImage.copy(_compressImage.getConfig(), true);
        Utils.bitmapToMat(bitmapMain, matMain);
        Imgproc.cvtColor(matMain, matMain, Imgproc.COLOR_BGR2HSV);
        Scalar avange = Core.mean(matMain);
        Log.d("caries", "Среднее HSV:" + avange);
        bitmapMain = HSVMask(bitmapMain, 0,80,0,255,255,(int) 135);
        Bitmap bitmapMask = FindContur(_compressImage);
        bitmapMain = UseMaskAndGetRidOfNoise(bitmapMain, bitmapMask);
        Utils.bitmapToMat(bitmapMain, matMain);
        avange = Core.mean(matMain);
        Log.d("caries", "Среднее HSV:" + avange);
        if (avange.val[2] > 1.45){ _tvStatus.setText("Кариес найден"); }
        else {_tvStatus.setText("Кариес не найден");}
        return new Diseases(bitmapMain, _tvStatus);
    }
    public static Diseases Gingivitis(Bitmap _compressImage, TextView _tvStatus){
        Log.d("CV", "Гингивит");
        Bitmap bitmapMain = _compressImage.copy(_compressImage.getConfig(), true);
        Mat matMain = new Mat();
        Utils.bitmapToMat(bitmapMain, matMain);
        Bitmap bitmapMask = FindContur(_compressImage);
        bitmapMask = HSVMask(bitmapMask,0,0,0,255,255,200);
        Imgproc.cvtColor(matMain, matMain, Imgproc.COLOR_BGR2HSV);
        Scalar avange = Core.mean(matMain);
        Log.d("Gingivitis", "Среднее HSV:" + avange);
        bitmapMain = HSVMask(bitmapMain, 115,(int) (120+(avange.val[1])*0.5),(int) (avange.val[2]-50),123,255,255);
        bitmapMain = UseMaskAndGetRidOfNoise(bitmapMain, bitmapMask);
        Utils.bitmapToMat(bitmapMain, matMain);
        avange = Core.mean(matMain);
        Log.d("Gingivitis", "Среднее HSV для проверки:" + avange);
        if (avange.val[2] > 3){ _tvStatus.setText("Гингивит найден"); }
        else {_tvStatus.setText("Гингивит не найден");}
        return new Diseases(bitmapMain, _tvStatus);
    }
    public static Diseases Gingivitis2(Bitmap _compressImage, TextView _tvStatus){
        Log.d("CV", "Гингивит2");
        Bitmap bitmapMain = _compressImage.copy(_compressImage.getConfig(), true);
        Mat matMain = new Mat();
        Utils.bitmapToMat(bitmapMain, matMain);
        Bitmap bitmapMask = HSVMask(bitmapMain,115,65,150,134,255,255);
        Imgproc.cvtColor(matMain, matMain, Imgproc.COLOR_BGR2HSV);
        Scalar avange = Core.mean(matMain);
        Log.d("Gingivitis2", "Среднее HSV:" + avange);
        bitmapMain = HSVMask(bitmapMain, 115,(int) (120+(avange.val[1])*0.5),(int) (avange.val[2]-50),123,255,255);
        bitmapMain = UseMaskAndGetRidOfNoise(bitmapMain, bitmapMask);
        Utils.bitmapToMat(bitmapMain, matMain);
        avange = Core.mean(matMain);
        Log.d("Gingivitis2", "Среднее HSV для проверки:" + avange);
        if (avange.val[2] > 3){ _tvStatus.setText("Гингивит найден"); }
        else {_tvStatus.setText("Гингивит не найден");}
        return new Diseases (bitmapMain, _tvStatus);
    }
}
