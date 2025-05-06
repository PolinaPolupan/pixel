#include <cstddef>
#include <string>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>
#include <unistd.h>

#include "com_example_mypixel_service_FilteringServiceNative.h"

using namespace std;
using namespace cv;

JNIEXPORT void JNICALL Java_com_example_mypixel_service_FilteringServiceNative_gaussianBlur
  (JNIEnv * env, jobject thisObject, jstring filename, jint sizeX, jint sizeY, jdouble sigmaX, jdouble sigmaY) {
  string filename_str = string(env->GetStringUTFChars(filename, nullptr));

  const Mat src = imread(filename_str, IMREAD_COLOR);
  if (src.empty()) {
    printf(" Error opening image\n");
    return;
  }

  Mat dst = src.clone();

  GaussianBlur( src, dst, Size(sizeX, sizeY), sigmaX, sigmaY);

  imwrite(filename_str, dst);
}

JNIEXPORT void JNICALL Java_com_example_mypixel_service_FilteringServiceNative_blur
  (JNIEnv * env, jobject, jstring filename, jint sizeX, jint sizeY) {
  string filename_str = string(env->GetStringUTFChars(filename, nullptr));

  const Mat src = imread(filename_str, IMREAD_COLOR);
    if (src.empty()) {
      printf(" Error opening image\n");
      return;
    }

    Mat dst = src.clone();

    blur(src, dst, Size(sizeX, sizeY));

    imwrite(filename_str, dst);
}

JNIEXPORT void JNICALL Java_com_example_mypixel_service_FilteringServiceNative_medianBlur
  (JNIEnv * env, jobject, jstring filename, jint ksize) {
  string filename_str = string(env->GetStringUTFChars(filename, nullptr));

  const Mat src = imread(filename_str, IMREAD_COLOR);
    if (src.empty()) {
      printf(" Error opening image\n");
      return;
    }

    Mat dst = src.clone();

    medianBlur(src, dst, ksize);

    imwrite(filename_str, dst);
}

JNIEXPORT void JNICALL Java_com_example_mypixel_service_FilteringServiceNative_bilateralFilter
  (JNIEnv * env, jobject, jstring filename, jint d, jdouble sigmaColor, jdouble sigmaSpace) {
  string filename_str = string(env->GetStringUTFChars(filename, nullptr));

  const Mat src = imread(filename_str, IMREAD_COLOR);
  if (src.empty()) {
    printf(" Error opening image\n");
    return;
  }

  Mat dst = src.clone();

  bilateralFilter(src, dst, d, sigmaColor, sigmaSpace);

  imwrite(filename_str, dst);
}

JNIEXPORT void JNICALL Java_com_example_mypixel_service_FilteringServiceNative_boxFilter
  (JNIEnv * env, jobject, jstring filename, jint ddepth, jint ksizeX, jint ksizeY) {
  string filename_str = string(env->GetStringUTFChars(filename, nullptr));

    const Mat src = imread(filename_str, IMREAD_COLOR);
    if (src.empty()) {
      printf(" Error opening image\n");
      return;
    }

    Mat dst = src.clone();

    boxFilter(src, dst, ddepth, Size(ksizeX, ksizeY));

    imwrite(filename_str, dst);
}
