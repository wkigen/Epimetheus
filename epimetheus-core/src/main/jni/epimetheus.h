//
// Created by Dell on 2018/3/29.
//

#ifndef EPIMETHEUS_EPIMETHEUS_H
#define EPIMETHEUS_EPIMETHEUS_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT bool JNICALL Java_com_github_wkigen_epimetheus_jni_EpimetheusJni_replaceMethod(JNIEnv *, jclass,jobject,jobject);

JNIEXPORT bool JNICALL Java_com_github_wkigen_epimetheus_jni_EpimetheusJni_deleteClass(JNIEnv *, jclass, jbyteArray,jint,jbyteArray,jint);

#ifdef __cplusplus
}
#endif

#endif //EPIMETHEUS_EPIMETHEUS_H
