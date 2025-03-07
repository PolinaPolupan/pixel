#include "com_example_mypixel_processor_Hello.h"
#include "com_example_mypixel_processor_OpenCv.h"

#include <opencv2/core.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/highgui.hpp>

#include <iostream>
#include <unistd.h>

JNIEXPORT void JNICALL Java_com_example_mypixel_processor_Hello_sayHello
  (JNIEnv *, jobject) {
    std::cout << "Hello from C++ !!" << std::endl;
}

JNIEXPORT void JNICALL Java_com_example_mypixel_processor_OpenCv_hi(JNIEnv *, jobject) {
    std::string image_path = "starry_night.jpg";

    // Print the current working directory
    char cwd[PATH_MAX];
    if (getcwd(cwd, sizeof(cwd)) != nullptr) {
        std::cout << "Current working directory: " << cwd << std::endl;
    }

    cv::Mat img = cv::imread(image_path, cv::IMREAD_COLOR);

    if(img.empty())
    {
        std::cout << "Could not read the image: " << image_path << std::endl;
    }

    cv::imshow("Display window", img);
    int k = cv::waitKey(0); // Wait for a keystroke in the window

    if(k == 's')
    {
        cv::imwrite("starry_night.png", img);
    }
}