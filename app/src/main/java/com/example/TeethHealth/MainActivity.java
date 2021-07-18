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
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;

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
    public Diseases disease;

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
                    finalBimap = FunctionsImage.HSVMask(bitmapToFilter, hMin.getProgress(), sMin.getProgress(), vMin.getProgress(), hMax.getProgress(), sMax.getProgress(), vMax.getProgress());
                    break;

                case (2):
                    finalBimap = FunctionsImage.Dilatation(bitmapToFilter, 5);
                    break;

                case (3):
                    finalBimap = FunctionsImage.Erosion(bitmapToFilter, 5);
                    break;

                case (4):
                    finalBimap = FunctionsImage.Teeth(bitmapToFilter);
                    break;

                case (5):
                    finalBimap = FunctionsImage.TeethColor(bitmapToFilter);
                    break;

                case (6):
                    finalBimap = FunctionsImage.TestFilter(bitmapToFilter);
                    break;

                case (7):
                    finalBimap = FunctionsImage.FindContur(bitmapToFilter);
                    break;

                case (8):
                    disease = FunctionsImage.Caries(bitmapToFilter, tvStatus);
                    finalBimap = Diseases.GetBitmap(disease);
                    tvStatus = Diseases.GetTvStatus(disease);
                    break;

                case (9):
                    disease = FunctionsImage.Gingivitis(bitmapToFilter, tvStatus);
                    finalBimap = Diseases.GetBitmap(disease);
                    tvStatus = Diseases.GetTvStatus(disease);
                    break;

                case (10):
                    disease = FunctionsImage.Gingivitis2(bitmapToFilter, tvStatus);
                    finalBimap = Diseases.GetBitmap(disease);
                    tvStatus = Diseases.GetTvStatus(disease);
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