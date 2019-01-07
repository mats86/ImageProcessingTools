#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/imgproc.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/dnn.hpp>
#include <tesseract/baseapi.h>
#include <leptonica/allheaders.h>
#include <tuple>
#include "decodeFunction.h"

using namespace cv;
using namespace dnn;

extern "C"
{
    JNIEXPORT jstring JNICALL
    Java_com_example_malattas_imageprocessingtools_TextRecognitionActivity_textRecognition(
            JNIEnv *env,
            jobject /* this */,
            /*jlong addrRgba,*/
            jstring jImPath) {

        const char *imPath = (*env).GetStringUTFChars(jImPath, NULL);
        jstring result;
//
//        std::string outText, test;
//        std::string imPath_ = "/storage/emulated/0/DCIM/ImageProcessing/test.jpg";
//
//        // initilize tesseract OCR engine
//        tesseract::TessBaseAPI *myOCR = new tesseract::TessBaseAPI();
//        __android_log_print(ANDROID_LOG_INFO, "__TESSERACT__", "Tesseract-ocr version: %s",
//                            myOCR->Version());
//
//        myOCR->Init("/storage/emulated/0/DCIM/ImageProcessing/", "eng");
//        __android_log_print(ANDROID_LOG_INFO, "__TESSERACT__", "Tesseract-ocr Init: %s", imPath);
//        __android_log_print(ANDROID_LOG_INFO, "__TESSERACT__", "Tesseract-ocr Init: %s",
//                            imPath_.c_str());
//
//        // Set Page segmentation mode to PSM_AUTO (3)
//        myOCR->SetPageSegMode(tesseract::PSM_AUTO);
//
//        // Open input image using OpenCV
//        Mat im_ = imread(imPath, IMREAD_COLOR);
//
//        // Set image data
//        myOCR->SetImage(im_.data, im_.cols, im_.rows, 3, (int) im_.step);
//
//        // Run Tesseract OCR on image
//        outText = std::string(myOCR->GetUTF8Text());
//        __android_log_print(ANDROID_LOG_INFO, "__TESSERACT__", "Tesseract-ocr OUT: %s",
//                            outText.c_str());
//
//        // result = (*env).NewStringUTF(outText.c_str());
//        //result = (*env).NewStringUTF(myOCR->Version());

        float confThreshold = 0.5;
        float nmsThreshold = 0.4;
        int inpWidth = 320;
        int inpHeight = 320;
        String model = "/storage/emulated/0/DCIM/ImageProcessing/tessdata/frozen_east_text_detection.pb";

        // Load network.
        Net net = readNet(model);
        __android_log_print(ANDROID_LOG_INFO, "__TESSERACT__", "Tesseract-ocr OUT: %s",
                            "Net loaded");
        std::vector<Mat> outs;
        std::vector<String> outNames(2);
        outNames[0] = "feature_fusion/Conv_7/Sigmoid";
        outNames[1] = "feature_fusion/concat_3";

        Mat frame, blob;

        frame = imread(imPath, IMREAD_COLOR);
        __android_log_print(ANDROID_LOG_INFO, "__TESSERACT__", "Tesseract-ocr OUT: %s",
                            "image read");
        blobFromImage(frame, blob, 1.0, Size(inpWidth, inpHeight), Scalar(123.68, 116.78, 103.94), true, false);
        __android_log_print(ANDROID_LOG_INFO, "__TESSERACT__", "Tesseract-ocr OUT: %s",
                            "blob frame");
        net.setInput(blob);
        __android_log_print(ANDROID_LOG_INFO, "__TESSERACT__", "Tesseract-ocr OUT: %s",
                            "set Input");
        net.forward(outs, outNames);
        __android_log_print(ANDROID_LOG_INFO, "__TESSERACT__", "Tesseract-ocr OUT: %s",
                            "forward");

        Mat scores = outs[0];
        Mat geometry = outs[1];

        // Decode predicted bounding boxes.
        std::vector<RotatedRect> boxes;
        std::vector<float> confidences;
        decode(scores, geometry, confThreshold, boxes, confidences);

        // Apply non-maximum suppression procedure.
        std::vector<int> indices;
        NMSBoxes(boxes, confidences, confThreshold, nmsThreshold, indices);
        // Render detections.
        Point2f ratio((float)frame.cols / inpWidth, (float)frame.rows / inpHeight);
        for (size_t i = 0; i < indices.size(); ++i)
        {
            RotatedRect& box = boxes[indices[i]];
            Point2f vertices[4];
            box.points(vertices);
            for (int j = 0; j < 4; ++j)
            {
                vertices[j].x *= ratio.x;
                vertices[j].y *= ratio.y;
            }
            for (int j = 0; j < 4; ++j)
                line(frame, vertices[j], vertices[(j + 1) % 4], Scalar(0, 255, 0), 2);
        }
        // Put efficiency information.
        std::vector<double> layersTimes;
        double freq = getTickFrequency() / 1000;
        double t = net.getPerfProfile(layersTimes) / freq;
        std::string label = format("Inference time: %.2f ms", t);
        imwrite(imPath, frame);
        __android_log_print(ANDROID_LOG_INFO, "__TESSERACT__", "Tesseract-ocr OUT: %s", label.c_str());

        result = (*env).NewStringUTF(imPath);
        return result;
    }
}

