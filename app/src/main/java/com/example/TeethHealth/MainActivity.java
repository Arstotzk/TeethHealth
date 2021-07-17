package com.example.TeethHealth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  {
    private final int PICKER = 1;
    private ImageView picView;
    private TextView tvHSV, tvStatus;
    public SeekBar hMin, sMin, vMin, hMax, sMax, vMax;
    public Bitmap bitmap1 = null;
    public Bitmap bitmapToFilter = null;
    public Bitmap finalBimap = null;
    public String imgPath = "";
    public boolean WifiCamWork = false;
    public boolean pauseByCam = false;

    String[] filterName = { "Settings", "HSV", "Dilatation", "Erosion", "Teeth", "TeethColor", "Test", "Contur", "Caries", "Gingivitis", "Gingivitis2"};
    public int filter = 0;
    public Switch operationOnImage = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    Mat imageMat = new Mat();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        if ( WifiCamWork == true){
            File storageDir = new File("/storage/emulated/0/DCIM/Photos/");
            String filePath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Photos";
            String fileName = "20210516131449.jpg";
            File f = new File(filePath,fileName);
            Log.d("FilePath","File:" + f);
            File[] listFiles = storageDir.listFiles();
            Log.d("FilePath","List:" + listFiles);

            long oldTime = 0;
            for (File file : listFiles) {
                String datas = file.getName();
                datas = datas.substring(0, datas.length() - 4);
                long newTime = Long.parseLong(datas);
                if (newTime > oldTime){
                    f = file;
                    oldTime = newTime;
                }
                Log.d("FilePath", "data:" + datas);
            }

            imgPath = f.getAbsolutePath();
            bitmap1 = BitmapFactory.decodeFile(imgPath);
            picView.setImageBitmap(bitmap1);
            WifiCamWork = false;
        }
    }
    public void onPause() {
        super.onPause();
        if (pauseByCam == true){
            WifiCamWork = true;
            pauseByCam = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvHSV = findViewById(R.id.tvHSV);
        tvStatus = findViewById(R.id.tvStatus);
        picView = (ImageView) findViewById(R.id.picture);


        hMin = (SeekBar)findViewById(R.id.hMin);
        hMin.setOnSeekBarChangeListener(seekBarChangeListener);
        sMin = (SeekBar)findViewById(R.id.sMin);
        sMin.setOnSeekBarChangeListener(seekBarChangeListener);
        vMin = (SeekBar)findViewById(R.id.vMin);
        vMin.setOnSeekBarChangeListener(seekBarChangeListener);
        hMax = (SeekBar)findViewById(R.id.hMax);
        hMax.setOnSeekBarChangeListener(seekBarChangeListener);
        sMax = (SeekBar)findViewById(R.id.sMax);
        sMax.setOnSeekBarChangeListener(seekBarChangeListener);
        vMax = (SeekBar)findViewById(R.id.vMax);
        vMax.setOnSeekBarChangeListener(seekBarChangeListener);

        operationOnImage = findViewById(R.id.switch1);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapterSpiner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, filterName);
        adapterSpiner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterSpiner);
        verifyStoragePermissions(this);
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Получаем выбранный объект
                String item = (String)parent.getItemAtPosition(position);
                switch (item) {
                    case ("Settings"):
                        operationOnImage.setVisibility(View.VISIBLE);
                        VisualElements.SetVisibles(View.GONE, new View []{hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus});
                        filter = 0;
                        break;
                    case ("HSV"):
                        VisualElements.SetVisibles(View.VISIBLE, new View []{hMin, sMin, vMin, hMax, sMax, vMax, tvHSV});
                        VisualElements.SetVisibles(View.GONE, new View []{tvStatus, operationOnImage});
                        filter = 1;
                        break;
                    case ("Dilatation"):
                        VisualElements.SetVisibles(View.GONE, new View []{hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage});
                        filter = 2;
                        break;
                    case ("Erosion"):
                        VisualElements.SetVisibles(View.GONE, new View []{hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage});
                        filter = 3;
                        break;
                    case ("Teeth"):
                        VisualElements.SetVisibles(View.GONE, new View []{hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage});
                        filter = 4;
                        break;
                    case ("TeethColor"):
                        VisualElements.SetVisibles(View.GONE, new View []{hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage});
                        filter = 5;
                        break;
                    case ("Test"):
                        VisualElements.SetVisibles(View.GONE, new View []{hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage});
                        filter = 6;
                        break;
                    case ("Contur"):
                        VisualElements.SetVisibles(View.GONE, new View []{hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage});
                        filter = 7;
                        break;
                    case ("Caries"):
                        tvStatus.setVisibility(View.VISIBLE);
                        VisualElements.SetVisibles(View.GONE, new View []{hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, operationOnImage});
                        filter = 8;
                        break;
                    case ("Gingivitis"):
                        tvStatus.setVisibility(View.VISIBLE);
                        VisualElements.SetVisibles(View.GONE, new View []{hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, operationOnImage});
                        filter = 9;
                        break;
                    case ("Gingivitis2"):
                        tvStatus.setVisibility(View.VISIBLE);
                        VisualElements.SetVisibles(View.GONE, new View []{hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, operationOnImage});
                        filter = 10;
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);

    }
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            showHSV();
        }
    };
    public void showHSV(){
        tvHSV.setText("hMin: " + hMin.getProgress() + ", sMin: " + sMin.getProgress() + ", vMin: " + vMin.getProgress() + ", hMax: " + hMax.getProgress() + ", sMax: " + sMax.getProgress() + ", vMax: " + vMax.getProgress());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //Срабатывает при выборе изображения из галереи
            if (requestCode == PICKER) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                try {
                    bitmap1 = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage));
                    Log.d("Bitmap catch", "Новая картинка из потока");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Mat mat = new Mat();
                Utils.bitmapToMat(bitmap1, mat);
                picView.setImageBitmap(bitmap1);
                if (bitmap1 != null) {
                    Log.d("Images", "Путь:" + bitmap1);
                }
            }
        }
    }
    public Bitmap ConvertToGray(Bitmap compressImage){
        Log.d("CV", "Before converting to black");
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
    public Bitmap HSVMask(Bitmap compressImage, int hMi, int sMi, int vMi, int hMa, int sMa, int vMa){
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
    public Bitmap Dilatation(Bitmap compressImage, int kernelSize){
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

    public Bitmap Erosion(Bitmap compressImage, int kernelSize){
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
    public Bitmap Teeth(Bitmap compressImage){
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
    public Bitmap TeethColor(Bitmap compressImage){
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
    public Bitmap TestFilter(Bitmap compressImage){
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
    public Bitmap FindContur (Bitmap compressImage){
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
            //Imgproc.drawContours(drawing, contours, i, new Scalar(255, 255, 255), Core.LINE_4, Core.LINE_8, hierarchy, 10, new Point());
        }
        Utils.matToBitmap(drawing,bitmapMain);
        return bitmapMain;
    }
    public Bitmap Caries (Bitmap compressImage){
        Log.d("CV", "Кариес");
        Mat matMain = new Mat();
        Mat matMask = new Mat();
        Mat matColor = new Mat();
        Bitmap bitmapMain = compressImage.copy(compressImage.getConfig(), true);
        Utils.bitmapToMat(bitmapMain, matMain);
        Utils.bitmapToMat(bitmapMain, matColor);
        Imgproc.cvtColor(matMain, matMain, Imgproc.COLOR_BGR2HSV);
        Scalar avange = Core.mean(matMain);
        Log.d("caries", "Среднее HSV:" + avange);
        bitmapMain = HSVMask(bitmapMain, 0,80,0,255,255,(int) 135);
        Bitmap bitmapMask = FindContur(compressImage);
        Utils.bitmapToMat(bitmapMask, matMask);
        Utils.bitmapToMat(bitmapMain, matMain);
        Core.bitwise_and(matMain, matMask, matMain);
        Core.bitwise_and(matColor, matMain, matColor);
        Utils.matToBitmap(matMain, bitmapMain);
        bitmapMain = Erosion(bitmapMain, 3);
        bitmapMain = Dilatation(bitmapMain, 4);
        Utils.bitmapToMat(bitmapMain, matMain);
        avange = Core.mean(matMain);
        Log.d("caries", "Среднее HSV:" + avange);
        if (avange.val[2] > 1.45){ tvStatus.setText("Кариес найден"); }
        else {tvStatus.setText("Кариес не найден");}
        return bitmapMain;
    }
    public Bitmap Gingivitis (Bitmap compressImage){
        Log.d("CV", "Гингивит");
        Bitmap bitmapMain = compressImage.copy(compressImage.getConfig(), true);
        Mat matMain = new Mat();
        Mat matMask = new Mat();
        Utils.bitmapToMat(bitmapMain, matMain);
        Bitmap bitmapMask = FindContur(compressImage);
        bitmapMask = HSVMask(bitmapMask,0,0,0,255,255,200);
        Imgproc.cvtColor(matMain, matMain, Imgproc.COLOR_BGR2HSV);
        Scalar avange = Core.mean(matMain);
        Log.d("Gingivitis", "Среднее HSV:" + avange);
        bitmapMain = HSVMask(bitmapMain, 115,(int) (120+(avange.val[1])*0.5),(int) (avange.val[2]-50),123,255,255);
        Utils.bitmapToMat(bitmapMask, matMask);
        Utils.bitmapToMat(bitmapMain, matMain);
        Core.bitwise_and(matMain, matMask, matMain);
        Utils.matToBitmap(matMain, bitmapMain);
        bitmapMain = Erosion(bitmapMain, 3);
        bitmapMain = Dilatation(bitmapMain, 4);
        Utils.bitmapToMat(bitmapMain, matMain);
        avange = Core.mean(matMain);
        Log.d("Gingivitis", "Среднее HSV для проверки:" + avange);
        if (avange.val[2] > 3){ tvStatus.setText("Гингивит найден"); }
        else {tvStatus.setText("Гингивит не найден");}
        return bitmapMain;
    }
    public Bitmap Gingivitis2 (Bitmap compressImage){
        Log.d("CV", "Гингивит2");
        Bitmap bitmapMain = compressImage.copy(compressImage.getConfig(), true);
        Mat matMain = new Mat();
        Mat matMask = new Mat();
        Utils.bitmapToMat(bitmapMain, matMain);
        Bitmap bitmapMask = HSVMask(bitmapMain,115,65,150,134,255,255);
        Imgproc.cvtColor(matMain, matMain, Imgproc.COLOR_BGR2HSV);
        Scalar avange = Core.mean(matMain);
        Log.d("Gingivitis2", "Среднее HSV:" + avange);
        bitmapMain = HSVMask(bitmapMain, 115,(int) (120+(avange.val[1])*0.5),(int) (avange.val[2]-50),123,255,255);
        Utils.bitmapToMat(bitmapMask, matMask);
        Utils.bitmapToMat(bitmapMain, matMain);
        Core.bitwise_and(matMain, matMask, matMain);
        Utils.matToBitmap(matMain, bitmapMain);
        bitmapMain = Erosion(bitmapMain, 3);
        bitmapMain = Dilatation(bitmapMain, 4);
        Utils.bitmapToMat(bitmapMain, matMain);
        avange = Core.mean(matMain);
        Log.d("Gingivitis2", "Среднее HSV для проверки:" + avange);
        if (avange.val[2] > 3){ tvStatus.setText("Гингивит найден"); }
        else {tvStatus.setText("Гингивит не найден");}
        return bitmapMain;
    }
    public void onClickToShow(View view){
        Log.d("Button", "Нажата");
        if (bitmap1 != null) {
            Log.d("Button", "Битмап не нулевой");
            Boolean switchState = operationOnImage.isChecked();
            if (switchState == true){
                if (finalBimap == null) {
                    bitmapToFilter = bitmap1.copy(bitmap1.getConfig(), true);
                }
                else {
                    bitmapToFilter = finalBimap.copy(finalBimap.getConfig(), true);
                }
            }
            else {
                bitmapToFilter = bitmap1.copy(bitmap1.getConfig(), true);
            }
            Log.d("Filter", "Фильтр номер:" + filter);
            switch (filter){
                case (0):
                    break;

                case (1):
                    finalBimap = HSVMask(bitmapToFilter, hMin.getProgress(), sMin.getProgress(), vMin.getProgress(), hMax.getProgress(), sMax.getProgress(), vMax.getProgress());
                    break;

                case (2):
                    finalBimap = Dilatation(bitmapToFilter, 5);
                    break;

                case (3):
                    finalBimap = Erosion(bitmapToFilter, 5);
                    break;

                case (4):
                    finalBimap = Teeth(bitmapToFilter);
                    break;

                case (5):
                    finalBimap = TeethColor(bitmapToFilter);
                    break;

                case (6):
                    finalBimap = TestFilter(bitmapToFilter);
                    break;

                case (7):
                    finalBimap = FindContur(bitmapToFilter);
                    break;
                case (8):
                    finalBimap = Caries(bitmapToFilter);
                    break;
                case (9):
                    finalBimap = Gingivitis(bitmapToFilter);
                    break;
                case (10):
                    finalBimap = Gingivitis2(bitmapToFilter);
                    break;
            }
            picView.setImageBitmap(finalBimap);
        }
    }
    public void onClickToTakePhotos(View view){
        String appPackageName = "com.tony.molink.zcf1000";
        Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(appPackageName);
        if (LaunchIntent != null){
            startActivity(LaunchIntent);
            pauseByCam = true;
        }
        else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }


    }

    public void onClickToPick(View view) {
        Intent pickIntent = new Intent();
        pickIntent.setType("image/*");
        pickIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), PICKER);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}