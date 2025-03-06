#include "com_example_mypixel_processor_Hello.h"

#include <iostream>

JNIEXPORT void JNICALL Java_com_example_mypixel_processor_Hello_sayHello
  (JNIEnv *, jobject) {
    std::cout << "Hello from C++ !!" << std::endl;
}
