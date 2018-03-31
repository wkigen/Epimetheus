package com.github.wkigen.epimetheus.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.dex.Dex;
import com.github.wkigen.epimetheus.jni.EpimetheusJni;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Dell on 2018/3/26.
 */

public class EpimetheusLoader {

    public final static String TAG = "EpimetheusLoader";

    public static boolean tryDalvikInstall(Context context, String dexPath){

        ZipFile apk = null;

        try {

            ApplicationInfo applicationInfo = context.getApplicationInfo();
            apk =  new ZipFile(applicationInfo.sourceDir);

            final String srcDexPath = "classes.dex";
            final String fixDexPath = context.getFilesDir().getAbsolutePath()+"/" + EpimetheusConstant.FIX_DEX_NAME;

            File outputFile = new File(fixDexPath);

            ZipEntry rawApkDexFileEntry = apk.getEntry(srcDexPath);

            if (rawApkDexFileEntry == null){
                EpimetheusLog.e(TAG,"apk classes dex entry is null");
                return false;
            }

            InputStream oldDexStream = null;
            InputStream patchDexStream = null;
            OutputStream extractedDexStream = null;

            try {
                oldDexStream = apk.getInputStream(rawApkDexFileEntry);
                patchDexStream = new FileInputStream(new File(dexPath));
                extractedDexStream = new FileOutputStream(outputFile);

                byte[] oldDexByte = Utils.readByte(oldDexStream);
                byte[] patchDexByte = Utils.readByte(patchDexStream);

                if (oldDexByte == null || patchDexByte == null){
                    EpimetheusLog.e(TAG,"can not read the dex");
                    return false;
                }

                if (EpimetheusJni.deleteClass(oldDexByte,oldDexByte.length,patchDexByte,patchDexByte.length)){
                    extractedDexStream.write(oldDexByte,0,oldDexByte.length);
                }else{
                    EpimetheusLog.e(TAG,"can not delete classDef");
                    return false;
                }

            } finally {
                if (oldDexStream != null){
                    oldDexStream.close();
                }
                if (patchDexStream != null){
                    patchDexStream.close();
                }
                if (extractedDexStream != null){
                    extractedDexStream.close();
                }
            }

        }catch (Exception e){
            EpimetheusLog.e(TAG,e.getMessage());
        }finally {
            try {
                if (apk != null){
                    apk.close();
                }
            }catch (Exception e){
                EpimetheusLog.e(TAG,"close the zip is fail:"+e.getMessage());
            }
        }

        return true;
    }


}
