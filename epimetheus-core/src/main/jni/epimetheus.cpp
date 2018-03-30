//
// Created by Dell on 2018/3/29.
//

#include "epimetheus.h"
#include "SimpleDexFile.h"
#include "Log.h"

#ifdef __cplusplus
extern "C"
{
#endif


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