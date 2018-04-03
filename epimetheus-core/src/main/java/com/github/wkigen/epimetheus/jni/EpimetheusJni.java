package com.github.wkigen.epimetheus.jni;

import java.lang.reflect.Method;

/**
 * Created by Dell on 2018/3/29.
 */

public class EpimetheusJni {

    static {
        System.loadLibrary("epimetheus");
    }

    public static native boolean replaceMethod(Method src,Method des);

    public static native boolean deleteClass(byte[] oldDex,int oldLen,byte[] patchDex,int patchLen);


}
