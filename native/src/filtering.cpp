#include <cstddef>
#include <string>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/imgproc.hpp>
#include <iostream>
#include <unistd.h>

#include "com_example_mypixel_service_FilteringService.h"

using namespace std;
using namespace cv;

JNIEXPORT void JNICALL Java_com_example_mypixel_service_FilteringService_gaussianBlur
  (JNIEnv * env, jobject thisObject, jstring filename) {
  string filename_str = string(env->GetStringUTFChars(filename, NULL));

  // Print the current working directory
  char cwd[PATH_MAX];
  if (getcwd(cwd, sizeof(cwd)) != nullptr) {
      std::cout << "Current working directory: " << cwd << std::endl;
  }

  Mat src; Mat dst;

  src = imread(string(cwd) + "/upload-image-dir/" + filename_str, IMREAD_COLOR );
  if (src.empty())
  {
    printf(" Error opening image\n");
    return;
  }

  dst = src.clone();

  GaussianBlur( src, dst, Size( 33, 33), 1, 1);

  imwrite(string(cwd) + "/upload-image-dir/" + filename_str, dst);
}