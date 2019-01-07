//
// Created by malattas on 05.11.2018.
//

#ifndef IMAGEPROCESSINGTOOLS_DECODEFUNCTION_H
#define IMAGEPROCESSINGTOOLS_DECODEFUNCTION_H

#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/imgproc.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/dnn.hpp>
#include <tesseract/baseapi.h>
#include <leptonica/allheaders.h>
#include <tuple>

using namespace cv;
using namespace dnn;

void decode(const Mat& scores, const Mat& geometry, float scoreThresh,
            std::vector<RotatedRect>& detections, std::vector<float>& confidences);

#endif //IMAGEPROCESSINGTOOLS_DECODEFUNCTION_H
