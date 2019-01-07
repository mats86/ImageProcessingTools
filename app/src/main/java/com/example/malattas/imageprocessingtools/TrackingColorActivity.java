package com.example.malattas.imageprocessingtools;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.example.malattas.imageprocessingtools.databinding.ActivityTrackingColorBinding;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TrackingColorActivity extends AppCompatActivity implements
        CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener
{

    private static final String TAG = "TrackingColorActivity";
    private MyOpenCvView mOpenCvCameraView;
    private List<Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;

    CameraBridgeViewBase cameraBridgeViewBase;
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(TrackingColorActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public TrackingColorActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    private Mat mRgba, Out;
    String color = "Red";

    private ActivityTrackingColorBinding binding;
    private Animation fabOpenAnimation, fabCloseAnimation;
    private boolean isFabMenuOpen = false;


    static {
        System.loadLibrary("native-lib");
    }

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_color);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tracking_color);
        binding.setFabHandler(new FabHandler());
        getAnimations();

        initViews();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
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

        binding.fabRed.startAnimation(fabCloseAnimation);
        binding.fabGreen.startAnimation(fabCloseAnimation);
        binding.fabBlue.startAnimation(fabCloseAnimation);
        binding.fabOrange.startAnimation(fabCloseAnimation);
        binding.fabGray.startAnimation(fabCloseAnimation);
        binding.fabYellow.startAnimation(fabCloseAnimation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    private void initViews() {
        mOpenCvCameraView = findViewById(R.id.trackingColorSurfView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }

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
        colorTracking(mRgba.getNativeObjAddr(), color);
        Out = mRgba;
        return mRgba;
    }

    public native void colorTracking(long addRgbaString, String color);


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG,"onTouch event");

        Imgproc.cvtColor(Out, Out, Imgproc.COLOR_RGBA2BGR);
        Imgcodecs.imwrite(getFileName(), Out);

        galleryAddPic();
        Toast.makeText(this, getFileName() + " saved", Toast.LENGTH_SHORT).show();
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

    private void getAnimations() {
        fabOpenAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabCloseAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_close);
    }

    private void expandFabMenu() {
        ViewCompat.animate(binding.fabColor).rotation(45.0F).withLayer().
                setDuration(300).setInterpolator(new OvershootInterpolator(10.0F)).start();
        binding.fabRed.startAnimation(fabOpenAnimation);
        binding.fabGreen.startAnimation(fabOpenAnimation);
        binding.fabBlue.startAnimation(fabOpenAnimation);
        binding.fabOrange.startAnimation(fabOpenAnimation);
        binding.fabGray.startAnimation(fabOpenAnimation);
        binding.fabYellow.startAnimation(fabOpenAnimation);
        binding.fabRed.setClickable(true);
        binding.fabGreen.setClickable(true);
        binding.fabBlue.setClickable(true);
        binding.fabOrange.setClickable(true);
        binding.fabGray.setClickable(true);
        binding.fabYellow.setClickable(true);
        isFabMenuOpen = true;
    }

    private void collapseFabMenu() {
        ViewCompat.animate(binding.fabColor).rotation(0.0F).withLayer().
                setDuration(300).setInterpolator(new OvershootInterpolator(10.0F)).start();
        binding.fabRed.startAnimation(fabCloseAnimation);
        binding.fabGreen.startAnimation(fabCloseAnimation);
        binding.fabBlue.startAnimation(fabCloseAnimation);
        binding.fabOrange.startAnimation(fabCloseAnimation);
        binding.fabGray.startAnimation(fabCloseAnimation);
        binding.fabYellow.startAnimation(fabCloseAnimation);
        binding.fabRed.setClickable(false);
        binding.fabGreen.setClickable(false);
        binding.fabBlue.setClickable(false);
        binding.fabOrange.setClickable(false);
        binding.fabGray.setClickable(false);
        binding.fabYellow.setClickable(false);
        isFabMenuOpen = false;
    }


    public class FabHandler {

        public void onPictureTakenClick(View view) {
            Imgproc.cvtColor(Out, Out, Imgproc.COLOR_RGBA2BGR);
            Imgcodecs.imwrite(getFileName(), Out);

        }

        public void onColorFabClick(View view) {

            if (isFabMenuOpen)
                collapseFabMenu();
            else
                expandFabMenu();
        }

        public void onRedFabClick(View view) {
            binding.fabColor.setBackgroundTintList(getColorStateList(R.color.colorRed));
            collapseFabMenu();

            color = getResources().getString(R.string.st_redColor);
            onToastColor(color);
        }

        public void onGreenFabClick(View view) {
            binding.fabColor.setBackgroundTintList(getColorStateList(R.color.colorGreen));
            collapseFabMenu();

            color = getResources().getString(R.string.st_greenColor);
            onToastColor(color);
        }

        public void onBlueFabClick(View view) {
            binding.fabColor.setBackgroundTintList(getColorStateList(R.color.colorBlue));
            collapseFabMenu();

            color = getResources().getString(R.string.st_blueColor);
            onToastColor(color);
        }

        public void onOrangeFabClick(View view) {
            binding.fabColor.setBackgroundTintList(getColorStateList(R.color.colorOrange));
            collapseFabMenu();

            color = getResources().getString(R.string.st_orangeColor);
            onToastColor(color);
        }

        public void onGrayFabClick(View view) {
            binding.fabColor.setBackgroundTintList(getColorStateList(R.color.colorGray));
            collapseFabMenu();

            color = getResources().getString(R.string.st_grayColor);
            onToastColor(color);
        }

        public void onYellowFabClick(View view) {
            binding.fabColor.setBackgroundTintList(getColorStateList(R.color.colorYellow));
            collapseFabMenu();

            color = getResources().getString(R.string.st_yellowColor);
            onToastColor(color);
        }

    }

    @Override
    public void onBackPressed() {

        if (isFabMenuOpen)
            collapseFabMenu();
        else
            super.onBackPressed();
    }

    private void onToastColor(String color) {
        SpannableString spannableString = new SpannableString(String.format(
                getResources().getString(R.string.st_color_tracking), color));
        spannableString.setSpan(new
                        ForegroundColorSpan(getResources().getColor(getResources().getIdentifier(
                "color"+color, "color",getPackageName()))),
                0,
                spannableString.length(),
                0);
        Toast.makeText(getApplicationContext(), spannableString,
                Toast.LENGTH_SHORT).show();
    }
}
