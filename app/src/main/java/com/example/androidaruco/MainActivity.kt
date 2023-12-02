package com.example.androidaruco

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import org.opencv.android.CameraActivity
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.Collections

class MainActivity : CameraActivity(), CvCameraViewListener2 {

    private var PERMISSION_CAMERA : String = android.Manifest.permission.CAMERA
    private var REQUEST_CODE : Int = 101
    private lateinit var cameraBridgeViewBase : CameraBridgeViewBase
    private lateinit var mIntermediateMat: Mat
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.fullcamera)
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ActivityCompat.requestPermissions(this, Array(1){Manifest.permission.CAMERA},101)
        getPermission(this, PERMISSION_CAMERA, REQUEST_CODE)
        cameraBridgeViewBase = findViewById(R.id.cameraview)
        cameraBridgeViewBase.setCvCameraViewListener(this)
        cameraBridgeViewBase.setCameraPermissionGranted()
        cameraBridgeViewBase.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY)
        cameraBridgeViewBase.visibility = SurfaceView.VISIBLE

        if(OpenCVLoader.initDebug()){
            Log.d("OPENCV:APP", "Successful load of opencv")
            cameraBridgeViewBase.enableView()

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.size > 0 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            getPermission(this,PERMISSION_CAMERA, REQUEST_CODE)
        }
    }

    override fun getCameraViewList(): List<CameraBridgeViewBase> {
        return Collections.singletonList(cameraBridgeViewBase)
    }
    override fun onCameraViewStarted(width: Int, height: Int) {
        mIntermediateMat = Mat()
    }

    override fun onCameraViewStopped() {
        mIntermediateMat.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat? {
        if (inputFrame != null) {
            val gray = inputFrame.gray()
            return inputFrame.rgba()
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        cameraBridgeViewBase.enableView()
    }

    override fun onDestroy() {
        cameraBridgeViewBase.disableView()
        super.onDestroy()
    }
}

// Getting Runtime Permission
fun getPermission(con: Context, permissionCamera: String, reqCode:Int) {
    if (ActivityCompat.checkSelfPermission(con, permissionCamera) ==
        PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(con, "Camera Permission Granted, we can use AR", Toast.LENGTH_LONG).show()
    } else if (ActivityCompat.shouldShowRequestPermissionRationale(
            con as Activity,
            permissionCamera
        )
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(con)
        val perm = Array(10) { permissionCamera }

        builder.setMessage("This application requires Camera")
            .setTitle("PERMISSION REQUIRED")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                ActivityCompat.requestPermissions(con , perm, reqCode)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    } else {
        val perm = Array(10) { permissionCamera }
        ActivityCompat.requestPermissions(con , perm, reqCode)
    }
}
