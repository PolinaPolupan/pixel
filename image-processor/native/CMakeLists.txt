cmake_minimum_required(VERSION 3.10)
project(native-processor)
set(CMAKE_CXX_STANDARD 17)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

file(GLOB SRCS src/*.c src/*.cpp)

add_library(native-processor SHARED ${SRCS})
target_link_libraries(native-processor ${JNI_LIBRARIES})
target_include_directories(native-processor PUBLIC ${CMAKE_SOURCE_DIR}/src/include)

enable_testing()
