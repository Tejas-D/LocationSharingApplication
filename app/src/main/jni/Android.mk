##################################################################################################################
#                                        SEECRYPT ANDROID JNI MODULES                                            #
##################################################################################################################

LOCAL_PATH := $(call my-dir)
common_CFLAGS := -I$(ANDROID_NDK_ROOT)\platforms\android-14\arch-arm\usr\include"
LOCAL_ARM_MODE := arm

##################################################################################################################
#                                              Real-time Stack                                                   #
##################################################################################################################

include $(CLEAR_VARS)
#LOCAL_LDLIBS    := -llog
LOCAL_MODULE    := sc3_rtstack
LOCAL_SRC_FILES := rt-stack/src/RS.c \
				   rt-stack/src/RSDebug.c \
				   com_example_tejas_finalv2_rtstack_RTStack.cpp
LOCAL_CFLAGS := -ffast-math -O3 -funroll-loops			   				   				   
include $(BUILD_SHARED_LIBRARY)