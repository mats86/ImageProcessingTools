#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

using namespace std;
using namespace cv;

extern "C"
{
void JNICALL Java_com_example_malattas_imageprocessingtools_TrackingColorActivity_colorTracking(
        JNIEnv *env, jobject instance,
        jlong addrRgba, jstring jColor);

void JNICALL
Java_com_example_malattas_imageprocessingtools_TrackingColorActivity_colorTracking(
        JNIEnv *env, jobject instance,
        jlong addrRgba, jstring jColor) {

    const char *color = (*env).GetStringUTFChars(jColor, NULL);
    (*env).ReleaseStringUTFChars(jColor, color);

    Mat &mRgb = *(Mat *) addrRgba;
    Mat blurred, hsv, mask, erodeElement, dilateElement;

    Scalar colorLower = Scalar(0, 0, 0);
    Scalar colorUpper = Scalar(0, 0, 0);
    Scalar textColor;

    String st_color;


    GaussianBlur(mRgb, blurred, Size(11, 11), 0);
    cvtColor(blurred, hsv, COLOR_RGB2HSV);


    if (string("Yellow") == color) {
        // __android_log_print(ANDROID_LOG_ERROR, "__COLOR_", "%s", color);
        colorLower = Scalar(20, 100, 100);
        colorUpper = Scalar(30, 255, 255);
        st_color = color;
        textColor = Scalar(255, 255, 0);
    } else if (string("Green") == color) {
        // __android_log_print(ANDROID_LOG_ERROR, "__COLOR_", "%s", color);
        colorLower = Scalar(29, 86, 6); // (45, 100, 50)
        colorUpper = Scalar(64, 255, 255); // (75, 255, 255)
        st_color = color;
        textColor = Scalar(0, 255, 0);
    } else if (string("Blue") == color) {
        // __android_log_print(ANDROID_LOG_ERROR, "__COLOR_", "%s", color);
        colorLower = Scalar(99, 115, 150);
        colorUpper = Scalar(110, 255, 255);
        st_color = color;
        textColor = Scalar(0, 0, 255);
    } else if (string("Gray") == color) {
        // __android_log_print(ANDROID_LOG_ERROR, "__COLOR_", "%s", color );
        colorLower = Scalar(0, 0, 0, 0);
        colorUpper = Scalar(180, 255, 30, 0);
        st_color = color;
        textColor = Scalar(128, 128, 128);
    } else if (string("Orange") == color) {
        // __android_log_print(ANDROID_LOG_ERROR, "__COLOR_", "%s", color);
        colorLower = Scalar(17, 15, 100);
        colorUpper = Scalar(50, 56, 200);
        st_color = color;
        textColor = Scalar(255, 165, 0);
    } else if (string("Red") == color) {
        // __android_log_print(ANDROID_LOG_ERROR, "__COLOR_", "%s", color);
        colorLower = Scalar(136, 87, 111);
        colorUpper = Scalar(180, 255, 255);
        st_color = color;
        textColor = Scalar(255, 0, 0);
    }


    inRange(hsv, colorLower, colorUpper, mask);
    erode(mask, mask, Mat(), Point(-1, -1), 2);
    dilate(mask, mask, Mat(), Point(-1, -1), 2);

    vector<vector<Point> > contours;
    vector<Vec4i> hierarchy;

    findContours(mask, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

    vector<vector<Point>> contours_ply(contours.size());
    vector<Rect> boundRect(contours.size());
    vector<Point2f> center(contours.size());
    vector<float> radius(contours.size());
    double _contourArea;

    int idx = 0;


    for (int i = 0; i < contours.size(); i++)
    {
        _contourArea = contourArea(contours[i]);
        if (_contourArea > 300)
        {
            idx++;
            approxPolyDP(Mat(contours[i]), contours_ply[i], 3, true);
            boundRect[i] = boundingRect( Mat(contours_ply[i]));
            minEnclosingCircle( contours_ply[i], center[i], radius[i]);

            // drawContours(mRgb, contours_ply, i, Scalar(0, 255, 255), 1, 8, vector<Vec4i>(), 0, Point());
            rectangle(mRgb, boundRect[i].tl(), boundRect[i].br(), Scalar(255, 255, 255), 2, 8, 0);
            //circle(mRgb, center[i], (int)radius[i], Scalar(0, 255, 255), 2, 8, 0);
            putText(mRgb, st_color + " # " + to_string(idx), boundRect[i].tl(), FONT_HERSHEY_SIMPLEX, 0.7, textColor, 3);
        }
    }
}

}
