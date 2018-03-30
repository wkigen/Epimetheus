package com.github.wkigen.epimetheus.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.jni.EpimetheusJni;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.utils.SystemUtils;
import com.github.wkigen.epimetheus.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Dell on 2018/3/26.
 */

public class EpimetheusDexLoader {

    private final static String TAG = "EpimetheusDexLoader";

    private EpimetheusDexLoader(){

    }

    public boolean tryHotInstall(){

        return true;
    }

    public static boolean tryColdInstall(Context context, String dexPath){

        ZipFile apk = null;
        ZipFile patch = null;

        try {

            ApplicationInfo applicationInfo = context.getApplicationInfo();

            apk =  new ZipFile(applicationInfo.sourceDir);
            patch = new ZipFile(dexPath);

            //-------------temp
            final String srcDexPath = "classes.dex";
            final String patchDexPath = "Patch.dex";
            final String fixDexPath = context.getFilesDir().getAbsolutePath()+"/" + EpimetheusConstant.FIX_DEX_NAME;

            File outputFile = new File(fixDexPath);

            ZipEntry rawApkFileEntry = apk.getEntry(srcDexPath);
            ZipEntry rawPatchFileEntry = patch.getEntry(patchDexPath);

            if (rawApkFileEntry == null){
                EpimetheusLog.e(TAG,"apk entry is null");
                return false;
            }

            if (rawPatchFileEntry == null){
                EpimetheusLog.e(TAG,"patch entry is null");
                return false;
            }

            InputStream oldStream = null;
            InputStream patchStream = null;
            OutputStream extractedStream = null;

            try {
                oldStream = apk.getInputStream(rawApkFileEntry);
                patchStream = patch.getInputStream(rawPatchFileEntry);
                extractedStream = new FileOutputStream(outputFile);

                byte[] oldDexByte = Utils.readByte(oldStream);
                byte[] patchDexByte = Utils.readByte(patchStream);

                if (oldDexByte == null || patchDexByte == null){
                    EpimetheusLog.e(TAG,"can not read the dex");
                    return false;
                }

                if (EpimetheusJni.deleteClass(oldDexByte,oldDexByte.length,patchDexByte,patchDexByte.length)){
                    extractedStream.write(oldDexByte,0,oldDexByte.length);
                }else{
                    EpimetheusLog.e(TAG,"can not delete classDef");
                    return false;
                }

            } finally {
                if (oldStream != null){
                    oldStream.close();
                }
                if (patchStream != null){
                    patchStream.close();

                }
                if (extractedStream != null){
                    extractedStream.close();
                }
            }

            if (SystemUtils.isART()){
                return tryColdArtInstall();
            }else {
                return tryColdDalvikInstall();
            }
        }catch (Exception e){
            EpimetheusLog.e(TAG,e.getMessage());
        }finally {
            try {
                if (apk != null){
                    apk.close();
                }
                if (patch != null){
                    patch.close();
                }
            }catch (Exception e){
                EpimetheusLog.e(TAG,"close the zip is fail:"+e.getMessage());
            }
        }

        return true;
    }

    private static boolean tryColdArtInstall(){
        return true;
    }

    private static boolean tryColdDalvikInstall(){
        long start = System.currentTimeMillis();


        EpimetheusLog.i(TAG,"tryColdDalvikInstall time :%d",(System.currentTimeMillis() - start));
        return true;
    }


}
