LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := epimetheus

LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog

MY_CPP_LIST := $(wildcard $(LOCAL_PATH)/*.cpp)

LOCAL_SRC_FILES := $(MY_CPP_LIST:$(LOCAL_PATH)/%=%)

LOCAL_C_INCLUDES := \
                    $(LOCAL_PATH)/zlib/src
					
include $(BUILD_SHARED_LIBRARY)