LOCAL_PATH:= $(call my-dir)

#----------------------------------------------------------------------
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := libnicoWnnGEngDic

LOCAL_SRC_FILES := \
	WnnEngDic.c

LOCAL_SHARED_LIBRARIES := 

LOCAL_STATIC_LIBRARIES :=

LOCAL_C_INCLUDES += \
	$(LOCAL_PATH)/../libwnnDictionary/include

LOCAL_CFLAGS += \
	-O

LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)
