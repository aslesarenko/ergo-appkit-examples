cmake_minimum_required(VERSION 3.9)
project(c_examples C)

set(CMAKE_C_STANDARD 11)

add_executable(c_examples freezecoin.c)
include_directories(".")
LINK_DIRECTORIES("...")
LINK_LIBRARIES(ergotool)

#include(graal_isolate.h
#        libergotool.h)