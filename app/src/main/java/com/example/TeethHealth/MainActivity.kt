package com.example.TeethHealth

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.io.FileNotFoundException


class MainActivity : AppCompatActivity() {
    private val PICKER = 1
    private var picView: ImageView? = null
    private var tvHSV: TextView? = null
    private var tvStatus: TextView? = null
    var hMin: SeekBar? = null
    var sMin: SeekBar? = null
    var vMin: SeekBar? = null
    var hMax: SeekBar? = null
    var sMax: SeekBar? = null
    var vMax: SeekBar? = null
    var bitmap1: Bitmap? = null
    var bitmapToFilter: Bitmap? = null
    var finalBimap: Bitmap? = null
    var imgPath = ""
    var WifiCamWork = false
    var pauseByCam = false
    var disease: Diseases? = null
    var filterName = arrayOf("Service","Settings", "HSV", "Dilatation", "Erosion", "Teeth", "TeethColor", "Test", "Contur", "Caries", "Gingivitis", "Gingivitis2")
    var filter = 0
    var operationOnImage: Switch? = null
    var filterButtons: LinearLayout? = null

    var sendImage: Button? = null

    var userName: String = ""
    var serviceAddress: String = ""
    var idDevice: String = ""


    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i("OpenCV", "OpenCV loaded successfully")
                    val imageMat = Mat()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
        if (WifiCamWork == true) {
            val storageDir = File("/storage/emulated/0/DCIM/Photos/")
            val filePath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Photos"
            val fileName = "20210516131449.jpg"
            var f = File(filePath, fileName)
            Log.d("FilePath", "File:$f")
            val listFiles = storageDir.listFiles()
            Log.d("FilePath", "List:$listFiles")
            var oldTime: Long = 0
            for (file in listFiles) {
                var datas = file.name
                datas = datas.substring(0, datas.length - 4)
                val newTime = datas.toLong()
                if (newTime > oldTime) {
                    f = file
                    oldTime = newTime
                }
                Log.d("FilePath", "data:$datas")
            }
            imgPath = f.absolutePath
            bitmap1 = BitmapFactory.decodeFile(imgPath)
            picView!!.setImageBitmap(bitmap1)
            WifiCamWork = false
        }
    }

    public override fun onPause() {
        super.onPause()
        if (pauseByCam == true) {
            WifiCamWork = true
            pauseByCam = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvHSV = findViewById(R.id.tvHSV)
        tvStatus = findViewById(R.id.tvStatus)
        picView = findViewById<View>(R.id.picture) as ImageView
        hMin = findViewById<View>(R.id.hMin) as SeekBar
        hMin!!.setOnSeekBarChangeListener(seekBarChangeListener)
        sMin = findViewById<View>(R.id.sMin) as SeekBar
        sMin!!.setOnSeekBarChangeListener(seekBarChangeListener)
        vMin = findViewById<View>(R.id.vMin) as SeekBar
        vMin!!.setOnSeekBarChangeListener(seekBarChangeListener)
        hMax = findViewById<View>(R.id.hMax) as SeekBar
        hMax!!.setOnSeekBarChangeListener(seekBarChangeListener)
        sMax = findViewById<View>(R.id.sMax) as SeekBar
        sMax!!.setOnSeekBarChangeListener(seekBarChangeListener)
        vMax = findViewById<View>(R.id.vMax) as SeekBar
        vMax!!.setOnSeekBarChangeListener(seekBarChangeListener)
        operationOnImage = findViewById(R.id.switch1)
        val spinner = findViewById<View>(R.id.spinner) as Spinner
        val adapterSpiner = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterName)
        adapterSpiner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpiner
        verifyStoragePermissions(this)
        val itemSelectedListener: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                // Получаем выбранный объект
                val item = parent.getItemAtPosition(position) as String
                when (item) {
                    "Settings" -> {
                        operationOnImage?.setVisibility(View.VISIBLE)
                        filterButtons?.setVisibility(View.VISIBLE)
                        VisualElements.SetVisibles(View.GONE, arrayOf<View?>(hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, sendImage))
                        filter = 0
                    }
                    "HSV" -> {
                        filterButtons?.setVisibility(View.VISIBLE)
                        VisualElements.SetVisibles(View.VISIBLE, arrayOf<View?>(hMin, sMin, vMin, hMax, sMax, vMax, tvHSV))
                        VisualElements.SetVisibles(View.GONE, arrayOf<View?>(tvStatus, operationOnImage, sendImage))
                        filter = 1
                    }
                    "Dilatation" -> {
                        filterButtons?.setVisibility(View.VISIBLE)
                        VisualElements.SetVisibles(View.GONE, arrayOf<View?>(hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage, sendImage))
                        filter = 2
                    }
                    "Erosion" -> {
                        filterButtons?.setVisibility(View.VISIBLE)
                        VisualElements.SetVisibles(View.GONE, arrayOf<View?>(hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage, sendImage))
                        filter = 3
                    }
                    "Teeth" -> {
                        filterButtons?.setVisibility(View.VISIBLE)
                        VisualElements.SetVisibles(View.GONE, arrayOf<View?>(hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage, sendImage))
                        filter = 4
                    }
                    "TeethColor" -> {
                        filterButtons?.setVisibility(View.VISIBLE)
                        VisualElements.SetVisibles(View.GONE, arrayOf<View?>(hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage, sendImage))
                        filter = 5
                    }
                    "Test" -> {
                        filterButtons?.setVisibility(View.VISIBLE)
                        VisualElements.SetVisibles(View.GONE, arrayOf<View?>(hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage, sendImage))
                        filter = 6
                    }
                    "Contur" -> {
                        filterButtons?.setVisibility(View.VISIBLE)
                        VisualElements.SetVisibles(View.GONE, arrayOf<View?>(hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage, sendImage))
                        filter = 7
                    }
                    "Caries" -> {
                        tvStatus?.setVisibility(View.VISIBLE)
                        filterButtons?.setVisibility(View.VISIBLE)
                        VisualElements.SetVisibles(View.GONE, arrayOf<View?>(hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, operationOnImage, sendImage))
                        filter = 8
                    }
                    "Gingivitis" -> {
                        tvStatus?.setVisibility(View.VISIBLE)
                        filterButtons?.setVisibility(View.VISIBLE)
                        VisualElements.SetVisibles(View.GONE, arrayOf<View?>(hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, operationOnImage, sendImage))
                        filter = 9
                    }
                    "Gingivitis2" -> {
                        tvStatus?.setVisibility(View.VISIBLE)
                        filterButtons?.setVisibility(View.VISIBLE)
                        VisualElements.SetVisibles(View.GONE, arrayOf<View?>(hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, operationOnImage, sendImage))
                        filter = 10
                    }
                    "Service" -> {
                        sendImage?.setVisibility(View.VISIBLE)
                        VisualElements.SetVisibles(View.GONE, arrayOf<View?>(hMin, sMin, vMin, hMax, sMax, vMax, tvHSV, tvStatus, operationOnImage, filterButtons))
                        filter = 11
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinner.onItemSelectedListener = itemSelectedListener

        filterButtons = findViewById<View>(R.id.filterButtons) as LinearLayout?

        sendImage = findViewById<View>(R.id.sendImage) as Button?

        userName = intent.extras!!.getString("userName")!!
        serviceAddress = intent.extras!!.getString("serviceAddress")!!
        idDevice = intent.extras!!.getString("idDevice")!!
    }

    private val seekBarChangeListener: SeekBar.OnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {
            showHSV()
        }
    }

    fun showHSV() {
        tvHSV!!.text = "hMin: " + hMin!!.progress + ", sMin: " + sMin!!.progress + ", vMin: " + vMin!!.progress + ", hMax: " + hMax!!.progress + ", sMax: " + sMax!!.progress + ", vMax: " + vMax!!.progress
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            //Срабатывает при выборе изображения из галереи
            if (requestCode == PICKER) {
                val selectedImage = data!!.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = contentResolver.query(selectedImage!!,
                        filePathColumn, null, null, null)
                cursor!!.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val picturePath = cursor.getString(columnIndex)
                cursor.close()
                try {
                    bitmap1 = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage))
                    Log.d("Bitmap catch", "Новая картинка из потока")
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                val mat = Mat()
                Utils.bitmapToMat(bitmap1, mat)
                picView!!.setImageBitmap(bitmap1)
                if (bitmap1 != null) {
                    Log.d("Images", "Путь:$bitmap1")
                }
            }
        }
    }

    fun onClickToShow(view: View?) {
        Log.d("Button", "Нажата")
        if (bitmap1 != null) {
            Log.d("Button", "Битмап не нулевой")
            val switchState = operationOnImage!!.isChecked
            bitmapToFilter = if (switchState == true) {
                if (finalBimap == null) {
                    bitmap1!!.copy(bitmap1!!.config, true)
                } else {
                    finalBimap!!.copy(finalBimap!!.config, true)
                }
            } else {
                bitmap1!!.copy(bitmap1!!.config, true)
            }
            Log.d("Filter", "Фильтр номер:$filter")
            when (filter) {
                0 -> {}
                1 -> finalBimap = FunctionsImage.HSVMask(bitmapToFilter, hMin!!.progress, sMin!!.progress, vMin!!.progress, hMax!!.progress, sMax!!.progress, vMax!!.progress)
                2 -> finalBimap = FunctionsImage.Dilatation(bitmapToFilter, 5)
                3 -> finalBimap = FunctionsImage.Erosion(bitmapToFilter, 5)
                4 -> finalBimap = FunctionsImage.Teeth(bitmapToFilter)
                5 -> finalBimap = FunctionsImage.TeethColor(bitmapToFilter)
                6 -> finalBimap = FunctionsImage.TestFilter(bitmapToFilter)
                7 -> finalBimap = FunctionsImage.FindContur(bitmapToFilter)
                8 -> {
                    disease = FunctionsImage.Caries(bitmapToFilter, tvStatus)
                    finalBimap = Diseases.GetBitmap(disease)
                    tvStatus = Diseases.GetTvStatus(disease)
                }
                9 -> {
                    disease = FunctionsImage.Gingivitis(bitmapToFilter, tvStatus)
                    finalBimap = Diseases.GetBitmap(disease)
                    tvStatus = Diseases.GetTvStatus(disease)
                }
                10 -> {
                    disease = FunctionsImage.Gingivitis2(bitmapToFilter, tvStatus)
                    finalBimap = Diseases.GetBitmap(disease)
                    tvStatus = Diseases.GetTvStatus(disease)
                }
                11 -> {}
            }
            picView!!.setImageBitmap(finalBimap)
        }
    }

    fun onClickToTakePhotos(view: View?) {
        val appPackageName = "com.tony.molink.zcf1000"
        val LaunchIntent = packageManager.getLaunchIntentForPackage(appPackageName)
        if (LaunchIntent != null) {
            startActivity(LaunchIntent)
            pauseByCam = true
        } else {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (anfe: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }
    }

    fun onClickToPick(view: View?) {
        val pickIntent = Intent()
        pickIntent.type = "image/*"
        pickIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(pickIntent, "Select Picture"), PICKER)
    }

    fun onClickToSendImage(view: View?) {
        val service = InteractionService(serviceAddress, applicationContext)
        val idDevice = Settings.Secure.getString(applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID)
        service.postImage(bitmap1!!, idDevice, userName)
    }

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        fun verifyStoragePermissions(activity: Activity?) {
            val permission = ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                )
            }
        }
    }
}