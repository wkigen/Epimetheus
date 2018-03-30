package com.github.wkigen.epimetheus.jni;

/**
 * Created by Dell on 2018/3/29.
 */

public class EpimetheusJni {

    static {
        System.loadLibrary("epimetheus");
    }


    public static native boolean deleteClass(byte[] oldDex,int oldLen,byte[] patchDex,int patchLen);


}
