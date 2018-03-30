#ifndef LOG_H_
#define LOG_H_

#include <android/log.h>

#define   LOG_TAG    "epimetheus_core"
#define   LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define   LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


#endif