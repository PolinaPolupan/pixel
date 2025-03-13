#include <cstddef>
#include <string>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>
#include <unistd.h>

#include "com_example_mypixel_service_FilteringService.h"

using namespace std;
using namespace cv;

JNIEXPORT void JNICALL Java_com_example_mypixel_service_FilteringService_gaussianBlurNative
  (JNIEnv * env, jobject thisObject, jstring filename, jint sizeX, jint sizeY, jdouble sigmaX, jdouble sigmaY) {
  string filename_str = string(env->GetStringUTFChars(filename, nullptr));

  const Mat src = imread(MYPIXEL_TEMP_IMAGE_STORAGE_DIR + filename_str, IMREAD_COLOR);
  if (src.empty()) {
    printf(" Error opening image\n");
    return;
  }

  Mat dst = src.clone();

  GaussianBlur( src, dst, Size( sizeX, sizeY), sigmaX, sigmaY);

  imwrite(MYPIXEL_TEMP_IMAGE_STORAGE_DIR + filename_str, dst);
}