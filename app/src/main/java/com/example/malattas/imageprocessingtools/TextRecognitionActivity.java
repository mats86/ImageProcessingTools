package com.example.malattas.imageprocessingtools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TextRecognitionActivity extends AppCompatActivity implements
        CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private static final String TAG = "TextRecognitionActivity";

    final Context context = this;

    private MyOpenCvView mOpenCvCameraView;
    private TextView textView, textView_tesseractVersion;
    private ImageView imageView_textRecognition;
    private String result;

    private Mat mRgba, Out;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(TextRecognitionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recognition);

        initViews();
    }

    private void initViews() {
        mOpenCvCameraView = findViewById(R.id.textRecognitionSurfView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        imageView_textRecognition = findViewById(R.id.iv_imageTextRecognition);
        textView = findViewById(R.id.textView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getApplicationContext(), "There is a problem in OpenCV",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    public native String textRecognition(String imPath);

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(width, height, CvType.CV_8UC4);
        Out = new Mat(width, height, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        Out.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        return mRgba;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG,"onTouch event");

        Imgproc.cvtColor(mRgba, Out, Imgproc.COLOR_RGBA2BGR);
        Imgcodecs.imwrite(getFileName(), Out);

        galleryAddPic();
        Toast.makeText(this, getFileName() + " saved", Toast.LENGTH_SHORT).show();

        String imPath = getFileName();
        result = textRecognition(imPath);
        textView.setText("Finish");
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setOnTouchListener(null);
        mOpenCvCameraView.setVisibility(View.INVISIBLE);
        imageView_textRecognition.setVisibility(View.VISIBLE);
        Uri imageUri = Uri.parse(result);
        imageView_textRecognition.setImageURI(imageUri);
        input_sitting();
        return false;
    }

    @SuppressLint("SimpleDateFormat")
    private String getFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateAndTime = sdf.format(new Date());

        return Environment.getExternalStorageDirectory().getPath() + "/" +
                Environment.DIRECTORY_DCIM  + "/ImageProcessing/"
                + currentDateAndTime + ".jpg";
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(getFileName());
        Uri connectUri = Uri.fromFile(f);
        mediaScanIntent.setData(connectUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void input_sitting(){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View input_promptsView = li.inflate(R.layout.input_prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(input_promptsView);


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
