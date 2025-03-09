#include "com_example_mypixel_processor_Hello.h"
#include "com_example_mypixel_processor_OpenCv.h"

#include <opencv2/core.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/highgui.hpp>
#include "opencv2/imgproc.hpp"


#include <iostream>
#include <unistd.h>

using namespace std;
using namespace cv;

int DELAY_CAPTION = 1500;
int DELAY_BLUR = 100;
int MAX_KERNEL_LENGTH = 31;

Mat src; Mat dst;
char window_name[] = "Smoothing Demo";

JNIEXPORT void JNICALL Java_com_example_mypixel_processor_Hello_sayHello
  (JNIEnv *, jobject) {
    std::cout << "Hello from C++ !!" << std::endl;
}

JNIEXPORT void JNICALL Java_com_example_mypixel_processor_OpenCv_hi(JNIEnv *, jobject) {
    std::string image_path = "starry_night.jpg";

    const char* filename = "starry_night.jpg";

    src = imread(filename , IMREAD_COLOR );
    if (src.empty())
    {
        printf(" Error opening image\n");
        return;
    }

    dst = src.clone();

    GaussianBlur( src, dst, Size( 11, 11), 1, 1);

    cv::namedWindow("Blurred Image", cv::WINDOW_AUTOSIZE);

    cv::imshow("Blurred Image", dst);

    cv::waitKey(0);
}


