//
// Created by Dell on 2018/3/29.
//

#include "epimetheus.h"
#include "SimpleDexFile.h"
#include "Log.h"
#include "ReplaceMethod.h"
#include <string.h>

#ifdef __cplusplus
extern "C"
{
#endif

static uint32_t ArtMethodSize = 0;

//算出ArtMethod大小
void calArtMethodSize(JNIEnv * env){
    jclass nativeStructsModelClass = env->FindClass("com/github/wkigen/epimetheus/jni/NativeStructsModel");

    size_t firMethod = (size_t)env->GetStaticMethodID(nativeStructsModelClass,"fun1","()V");
    size_t secMethod = (size_t)env->GetStaticMethodID(nativeStructsModelClass,"fun2","()V");

    ArtMethodSize = secMethod - firMethod;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    calArtMethodSize(env);

    result = JNI_VERSION_1_4;

    return result;
}


JNIEXPORT bool JNICALL Java_com_github_wkigen_epimetheus_jni_EpimetheusJni_replaceMethod(JNIEnv * env, jclass thiz,
    jobject src, jobject dest){

    uint8_t* smeth = (uint8_t *) env->FromReflectedMethod(src);
    uint8_t* dmeth = (uint8_t *) env->FromReflectedMethod(dest);
    memcpy(smeth,dmeth,ArtMethodSize);

    return true;
}


JNIEXPORT bool JNICALL Java_com_github_wkigen_epimetheus_jni_EpimetheusJni_deleteClass(JNIEnv * env, jclass thiz,
    jbyteArray oldDex,jint oldLen,jbyteArray pathcDex,jint patchLen){

    unsigned char * oldDexData = (unsigned char * )env->GetByteArrayElements(oldDex, 0);
    unsigned char * patchDexData = (unsigned char * )env->GetByteArrayElements(pathcDex, 0);

    if(oldDexData == NULL || patchDexData ==NULL){
        LOGE("oldDexData or patchDexData is null");
        return false;
    }

    bool res = deleteClassDef(oldDexData,oldLen,patchDexData,patchLen);

    env->ReleaseByteArrayElements(oldDex,(jbyte*)oldDexData,0);
    env->ReleaseByteArrayElements(pathcDex,(jbyte*)patchDexData,0);

    return res;
}


#ifdef __cplusplus
}
#endif