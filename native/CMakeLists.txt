cmake_minimum_required(VERSION 3.10)
project(native-processor)
set(CMAKE_CXX_STANDARD 17)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

file(GLOB SRCS src/*.c src/*.cpp)

add_library(native-processor SHARED ${SRCS})
target_link_libraries(native-processor ${JNI_LIBRARIES})
target_include_directories(native-processor PUBLIC ${CMAKE_SOURCE_DIR}/src/include)

find_package(OpenCV REQUIRED)
include_directories(${OpenCV_INCLUDE_DIRS})
target_link_libraries(native-processor ${OpenCV_LIBS})

get_filename_component(MYPIXEL_DIR ${CMAKE_CURRENT_SOURCE_DIR} DIRECTORY)
target_compile_definitions(native-processor PRIVATE
        MYPIXEL_SOURCE_DIR="${MYPIXEL_DIR}"
        MYPIXEL_IMAGE_STORAGE_DIR="${MYPIXEL_DIR}/upload-image-dir/"
        MYPIXEL_TEMP_IMAGE_STORAGE_DIR="${MYPIXEL_DIR}/temp-image-dir/"
        PROJECT_SOURCE_DIR="${CMAKE_CURRENT_SOURCE_DIR}"
        PROJECT_BINARY_DIR="${CMAKE_BINARY_DIR}"
        CURRENT_DIR="${CMAKE_CURRENT_SOURCE_DIR}"
)

enable_testing()
