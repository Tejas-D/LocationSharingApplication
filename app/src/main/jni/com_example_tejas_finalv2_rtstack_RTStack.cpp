/**
 *
 * librtstack.cpp
 *
 * RTStack library JNI interface
 *
 *  Created on: 06 Nov 2013
 *      Author: Joshua Leibstein
 *
 */

#include <string.h>
#include <stdlib.h>

#pragma GCC visibility push(default)

#include "com_example_tejas_finalv2_rtstack_RTStack.h"

#pragma GCC visibility pop

#include "rt-stack/src/RS.h"

#if (defined DEBUG)
#include "rt-stack/src/RSDebug.h"
#endif

//#include <android/log.h>

//#define TAG "rtstack_jni"
//#define L_ERROR(...)    __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

const char *classPath = "com/example/tejas/finalv2/rtstack/RTStack";
static JavaVM *g_jvm;
static jobject g_RTStackObject;
static jmethodID MID_onTxRawData_callback;
static jmethodID MID_onRxStreamData_callback;
static jmethodID MID_onRxControlData_callback;
static jmethodID MID_onError_callback;
static jmethodID MID_onSessionStateChange_callback;

jint createGlobalRtstackReference(JNIEnv *env, jclass cls) {
    jmethodID constr = env->GetMethodID(cls, "<init>", "()V");
    if (!constr) {
        //L_ERROR("ONLOAD: failed to get %s constructor", classPath);
        return JNI_ERR;
    }

    jobject obj = env->NewObject(cls, constr);
    if (!obj) {
        //L_ERROR("ONLOAD: failed to create a %s object", classPath);
        return JNI_ERR;
    }
    g_RTStackObject = env->NewGlobalRef(obj);
    return JNI_OK;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_jvm = vm;
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    } else {
        jclass cls = env->FindClass(classPath);
        createGlobalRtstackReference(env, cls);
        return JNI_VERSION_1_6;
    }
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_initIds
(JNIEnv
*env,
jclass cls
)
{
MID_onTxRawData_callback = env->GetStaticMethodID(cls, "onTxRawData", "(J[BI)B");
MID_onRxStreamData_callback = env->GetStaticMethodID(cls, "onRxStreamData", "(JI[BI)V");
MID_onRxControlData_callback = env->GetStaticMethodID(cls, "onRxControlData", "(JIB[BI)V");
MID_onError_callback = env->GetStaticMethodID(cls, "onError", "(JI)V");
MID_onSessionStateChange_callback = env->GetStaticMethodID(cls, "onSessionStateChange", "(JI)V");
}

void bindEnvContext(JNIEnv * *env) {
    int status = g_jvm->GetEnv((void **) &(*env), JNI_VERSION_1_6);
    if (status == JNI_EDETACHED) {
        //L_ERROR("bindEnvContext: jvm context detached use attachCurrentThread!!! %i", status);
    } else if (status == JNI_EVERSION) {
        //L_ERROR("bindEnvContext: jvm version not supported!!! %i", status);
    } else if (status == JNI_OK) {
        //L_ERROR("bindEnvContext: successfully attached vm context!!! %i", status);
    }
}

JNIEXPORT jlong

JNICALL Java_com_example_tejas_finalv2_rtstack_RTStack_doCreateNewSession
        (JNIEnv *env, jclass cls, jlong sessionId, jlong arg) {
    if (g_RTStackObject == NULL) {
        //L_ERROR("init recaching g_RTstackObject");
        createGlobalRtstackReference(env, cls);
    }

    RSSession_t *sessionPtr = RS_CreateNewSession((uint64_t) sessionId, (void *) arg);

    return (jlong) sessionPtr;
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_doFreeSession
(JNIEnv
*env,
jclass cls, jlong
sessionPtr)
{
(env)->
DeleteGlobalRef(g_RTStackObject);
g_RTStackObject = NULL;
RS_FreeSession((RSSession_t
*) sessionPtr);
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_doConnect
(JNIEnv
*env,
jclass cls, jlong
sessionPtr)
{
RS_Connect((RSSession_t
*) sessionPtr);
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_doEndSession
(JNIEnv
*env,
jclass cls, jlong
sessionPtr)
{
RS_EndSession((RSSession_t
*) sessionPtr);
}

/*******************************/
/********** CALLBACKS **********/
/*******************************/

uint8_t TxRawDataCallback(RSSession_t *pSession, uint8_t *pbData, uint32_t iLen, void *pArg) {
    JNIEnv *env;

    bindEnvContext(&env);
    if (env == NULL) {
        return 1;
    }

    jclass g_clazz = env->GetObjectClass(g_RTStackObject);

    jbyteArray out_array = env->NewByteArray((jsize) iLen);
    env->SetByteArrayRegion(out_array, 0, (jsize) iLen, (jbyte *) pbData);

    jbyte status = env->CallStaticByteMethod(g_clazz, MID_onTxRawData_callback, (jlong) pSession,
                                             out_array, (jint) iLen);

    env->DeleteLocalRef(out_array);

    return (uint8_t) status;
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_doSetTxRawDataCallback
(JNIEnv
*env,
jclass cls, jlong
sessionPtr)
{
RS_SetTxRawDataCallback((RSSession_t
*)sessionPtr, TxRawDataCallback);
}

void RxStreamDataCallback(RSSession_t *pSession, uint32_t iSeq, uint8_t *pbData, uint32_t iLen,
                          void *pArg) {
    JNIEnv *env;

    bindEnvContext(&env);
    if (env == NULL) {
        return;
    }

    jclass g_clazz = env->GetObjectClass(g_RTStackObject);

    jbyteArray out_array = env->NewByteArray((jsize) iLen);
    env->SetByteArrayRegion(out_array, 0, (jsize) iLen, (jbyte *) pbData);

    env->CallStaticVoidMethod(g_clazz, MID_onRxStreamData_callback, (jlong) pSession, (jint) iSeq,
                              out_array, (jint) iLen);

    env->DeleteLocalRef(out_array);
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_doSetRxStreamDataCallback
(JNIEnv
*env,
jclass cls, jlong
sessionPtr)
{
RS_SetRxStreamDataCallback((RSSession_t
*)sessionPtr, RxStreamDataCallback);
}

void RxControlDataCallback(RSSession_t *pSession, uint32_t iSeq, uint8_t bType, uint8_t *pbData,
                           uint32_t iLen, void *pArg) {
    JNIEnv *env;

    bindEnvContext(&env);
    if (env == NULL) {
        return;
    }

    jclass g_clazz = env->GetObjectClass(g_RTStackObject);

    jbyteArray out_array = env->NewByteArray((jsize) iLen);
    env->SetByteArrayRegion(out_array, 0, (jsize) iLen, (jbyte *) pbData);

    env->CallStaticVoidMethod(g_clazz, MID_onRxControlData_callback, (jlong) pSession, (jint) iSeq,
                              (jbyte) bType, out_array, (jint) iLen);
    env->DeleteLocalRef(out_array);
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_doSetRxControlDataCallback
(JNIEnv
*env,
jclass cls, jlong
sessionPtr)
{
RS_SetRxControlDataCallback((RSSession_t
*)sessionPtr, RxControlDataCallback);
}

void ErrorCallback(RSSession_t *pSession, RSError_t bError, void *pArg) {
    JNIEnv *env;

    bindEnvContext(&env);
    if (env == NULL) {
        return;
    }

    jclass g_clazz = env->GetObjectClass(g_RTStackObject);

    env->CallStaticVoidMethod(g_clazz, MID_onError_callback, (jlong) pSession, (jint) bError);
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_doSetErrorCallback
(JNIEnv
*env,
jclass cls, jlong
sessionPtr)
{
RS_SetErrorCallback((RSSession_t
*)sessionPtr, ErrorCallback);
}

void StateChangeCallback(RSSession_t *pSession, RSSessionState_t bState, void *pArg) {
    JNIEnv *env;

    bindEnvContext(&env);
    if (env == NULL) {
        return;
    }

    jclass g_clazz = env->GetObjectClass(g_RTStackObject);

    env->CallStaticVoidMethod(g_clazz, MID_onSessionStateChange_callback, (jlong) pSession,
                              (jint) bState);
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_doSetStateChangeCallback
(JNIEnv
*env,
jclass cls, jlong
sessionPtr)
{
RS_SetStateChangeCallback((RSSession_t
*)sessionPtr, StateChangeCallback);
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_doOneSecondTick
(JNIEnv
*env,
jclass cls, jlong
sessionPtr)
{
RS_1sTick((RSSession_t
*) sessionPtr);
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_doPutTxStreamData
(JNIEnv
*env,
jclass cls, jlong
sessionPtr,
jbyteArray data, jint
len)
{
jbyte *b_data = env->GetByteArrayElements(data, NULL);

RS_TxStreamData((RSSession_t
*) sessionPtr, (uint8_t*) b_data, (uint16_t) len);

env->
ReleaseByteArrayElements(data, b_data,
0);
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_doPutRxRawData
(JNIEnv
*env,
jclass cls, jlong
sessionPtr,
jbyteArray data, jint
len)
{
jbyte *b_data = env->GetByteArrayElements(data, NULL);

RS_PutRxRawData((RSSession_t
*)sessionPtr, (uint8_t*)b_data, (uint16_t) len);

env->
ReleaseByteArrayElements(data, b_data,
0);
}

JNIEXPORT void JNICALL
Java_com_example_tejas_finalv2_rtstack_RTStack_doPutTxControlData
(JNIEnv
*env,
jclass cls, jlong
sessionPtr,
jbyte type, jbyteArray
data,
jint len
)
{
jbyte *b_data = env->GetByteArrayElements(data, NULL);

RS_TxControlData((RSSession_t
*)sessionPtr,
uint8_t(type), (uint8_t
*) b_data, (uint16_t) len);

env->
ReleaseByteArrayElements(data, b_data,
0);
}

#if (defined DEBUG)
void DebugCallback(char* sDebug, uint16_t iLength) 
{
	JNIEnv* env;
	
	bindEnvContext(&env);
    if (env == NULL)
    {
        return;
    }

    jclass g_clazz = env->GetObjectClass(g_RTStackObject);
    
    jmethodID mid = env->GetStaticMethodID(g_clazz, "onRTStackDebug", "([BI)V");

    if (mid == 0)
        return;
		
	jbyteArray out_array = env->NewByteArray((jsize) iLength);
    env->SetByteArrayRegion(out_array, 0, (jsize) iLength, (jbyte*) sDebug);

    env->CallStaticVoidMethod(g_clazz, mid, out_array, (jint) iLength);
    
	env->DeleteLocalRef(out_array);
}

JNIEXPORT void JNICALL Java_com_example_tejas_finalv2_rtstack_RTStack_doSetDebugCallback
  (JNIEnv *, jclass, jlong)
{
	RSDebug_Init(DebugCallback);
}
#endif
