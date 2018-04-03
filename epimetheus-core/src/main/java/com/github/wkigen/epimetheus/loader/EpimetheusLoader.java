package com.github.wkigen.epimetheus.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.dex.Dex;
import com.github.wkigen.epimetheus.jni.EpimetheusJni;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.utils.SystemUtils;
import com.github.wkigen.epimetheus.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import dalvik.system.DexFile;

/**
 * Created by Dell on 2018/3/26.
 */

public class EpimetheusLoader {

    public final static String TAG = "EpimetheusLoader";

    public static boolean tryHotInstall(Context context, String dexPath,String fixDexOptPath,String patchClassName,String patchMethodName){

        try{

            File patchDexFile = new File(dexPath);
            if (!patchDexFile.exists())
                return false;

            File optimizedFile= new File(fixDexOptPath,patchDexFile.getName());
            if (optimizedFile.exists())
                optimizedFile.delete();

            final DexFile dexFile = DexFile.loadDex(dexPath,optimizedFile.getAbsolutePath(), Context.MODE_PRIVATE);

            ClassLoader patchClassLoader = new ClassLoader(context.getClassLoader()) {
                @Override
                protected Class<?> findClass(String className)
                        throws ClassNotFoundException {
                    Class<?> clazz = dexFile.loadClass(className, this);
                    if (clazz == null) {
                        return Class.forName(className);
                    }
                    if (clazz == null) {
                        throw new ClassNotFoundException(className);
                    }
                    return clazz;
                }
            };

            Enumeration<String> entrys = dexFile.entries();
            Class<?> patchClazz = null;
            while (entrys.hasMoreElements()) {
                String entry = entrys.nextElement();
                if (entry.equals(patchClassName) ) {
                    patchClazz = dexFile.loadClass(entry, patchClassLoader);
                    if (patchClazz != null) {
                        Method[] methods = patchClazz.getDeclaredMethods();
                        for (Method patchMethod : methods) {
                           if (patchMethod.getName().equals(patchMethodName)){
                               Class<?> willFixClazz = context.getClassLoader().loadClass(patchClassName);
                               Method willFixMethod = willFixClazz.getDeclaredMethod(patchMethodName,patchMethod.getParameterTypes());
                               EpimetheusJni.replaceMethod(willFixMethod,patchMethod);

//                               Field classLoaderField = patchClazz.getDeclaredField("classLoader");
//                               classLoaderField.setAccessible(true);
//                               classLoaderField.set(patchClazz,context.getClassLoader());
                               break;
                           }
                        }
                        break;
                    }
                }
            }
        }catch (Exception e){
            EpimetheusLog.w(TAG,e.getMessage());
        }
        return true;
    }
    public static boolean tryArtInstall(Context context, String dexPath) {
        ZipFile apk = null;
        InputStream patchDexStream = null;
        ZipOutputStream zipOutputStream = null;
        byte[] buffer=new byte[1024];
        int count =0;
        try {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            apk =  new ZipFile(applicationInfo.sourceDir);

            final String fixDexPath = context.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH +"/"+ EpimetheusConstant.FIX_ZIP_NAME;

            patchDexStream = new FileInputStream(dexPath);

            CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(fixDexPath), new CRC32());
            zipOutputStream = new ZipOutputStream(cos);

            //先写入补丁dex
            ZipEntry patchEntry = new ZipEntry("classes.dex");
            zipOutputStream.putNextEntry(patchEntry);
            while ((count = patchDexStream.read(buffer, 0, 1024)) != -1) {
                zipOutputStream.write(buffer, 0, count);
            }
            zipOutputStream.closeEntry();

            //写入apk中的dex
            for (Enumeration<? extends ZipEntry> entries = apk.entries(); entries.hasMoreElements();) {
                ZipEntry apkDexEntry = (ZipEntry) entries.nextElement();
                String zipEntryName = apkDexEntry.getName();

                if(zipEntryName.startsWith(EpimetheusConstant.DEX_PREFIX) && zipEntryName.endsWith(EpimetheusConstant.DEX_SUFFIX)){

                    String index = zipEntryName.substring(EpimetheusConstant.DEX_PREFIX.length(),zipEntryName.length()-EpimetheusConstant.DEX_SUFFIX.length());
                    Integer idx = index.length() == 0 ? 1 : Integer.parseInt(index)+1;
                    String dexName = EpimetheusConstant.DEX_PREFIX+ idx + EpimetheusConstant.DEX_SUFFIX;

                    InputStream orgDexInputStream = apk.getInputStream(apkDexEntry);

                    ZipEntry orgDexEntry = new ZipEntry(dexName);
                    zipOutputStream.putNextEntry(orgDexEntry);
                    while ((count = orgDexInputStream.read(buffer, 0, 1024)) != -1) {
                        zipOutputStream.write(buffer, 0, count);
                    }
                    zipOutputStream.closeEntry();
                    orgDexInputStream.close();

                }
            }

        }catch (Exception e){
            EpimetheusLog.e(TAG,e.getMessage());
        }finally {
            try {
                if (zipOutputStream !=null)
                    zipOutputStream.close();
                if (patchDexStream != null)
                    patchDexStream.close();
                if (apk != null){
                    apk.close();
                }
            }catch (Exception e){
                EpimetheusLog.e(TAG,"close the zip is fail:"+e.getMessage());
            }
        }
        return true;
    }

    public static boolean tryDalvikInstall(Context context, String dexPath){
        ZipFile apk = null;
        InputStream oldDexStream = null;
        InputStream patchDexStream = null;
        OutputStream extractedDexStream = null;

        try {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            apk =  new ZipFile(applicationInfo.sourceDir);

            final String srcDexPath = "classes.dex";
            final String fixDexPath = context.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH +"/"+ EpimetheusConstant.FIX_DEX_NAME;

            File outputFile = new File(fixDexPath);

            ZipEntry rawApkDexFileEntry = apk.getEntry(srcDexPath);

            if (rawApkDexFileEntry == null){
                EpimetheusLog.e(TAG,"apk classes dex entry is null");
                return false;
            }

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
                extractedDexStream.write(oldDexByte);
            }else{
                EpimetheusLog.e(TAG,"can not delete classDef");
                return false;
            }

        }catch (Exception e){
            EpimetheusLog.e(TAG,e.getMessage());
        }finally {
            try {
                if (oldDexStream != null){
                    oldDexStream.close();
                }
                if (patchDexStream != null){
                    patchDexStream.close();
                }
                if (extractedDexStream != null){
                    extractedDexStream.close();
                }
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
