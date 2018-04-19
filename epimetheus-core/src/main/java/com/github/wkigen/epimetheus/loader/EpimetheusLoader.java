package com.github.wkigen.epimetheus.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.github.wkigen.epimetheus.EpimetheusManager;
import com.github.wkigen.epimetheus.annotation.FIXMETHOD;
import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.jni.EpimetheusJni;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.patch.EpimetheusPatch;
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
import java.util.List;
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

    public static boolean tryHotInstall(Context context, String dexPath,String fixDexOptPath,List<EpimetheusPatch.FixClassInfo> fixClassList){
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
                for (EpimetheusPatch.FixClassInfo fixClassInfo : fixClassList){
                    if (entry.equals(fixClassInfo.name) ) {
                        patchClazz = dexFile.loadClass(entry, patchClassLoader);
                        if (patchClazz != null) {

                            if (SystemUtils.isART()){
                                //同包名下的权限问题
                                Field classLoaderField = patchClazz.getClass().getDeclaredField("classLoader");
                                classLoaderField.setAccessible(true);
                                classLoaderField.set(patchClazz,context.getClassLoader());
                            }

                            Method[] methods = patchClazz.getDeclaredMethods();
                            for (Method patchMethod : methods) {
                                for (EpimetheusPatch.FixMethodInfo fixMethodInfo:fixClassInfo.methods){
                                    if (patchMethod.getName().equals(fixMethodInfo.name)){
                                        Class<?>[]  patchMethodParams = patchMethod.getParameterTypes();
                                        if (patchMethodParams.length == fixMethodInfo.params.size()){
                                            int index = 0;
                                            boolean isSame = true;
                                            for (Class clazz : patchMethodParams){
                                                if (!clazz.getName().equals(fixMethodInfo.params.get(index))){
                                                    isSame = false;
                                                }
                                            }
                                            if (isSame){
                                                Class<?> willFixClazz = context.getClassLoader().loadClass(fixClassInfo.name);
                                                Method willFixMethod = willFixClazz.getDeclaredMethod(patchMethod.getName(), patchMethod.getParameterTypes());
                                                EpimetheusJni.replaceMethod(willFixMethod, patchMethod);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }


                        }
                    }
                }
            }
        }catch (Exception e){
            EpimetheusLog.w(TAG,e.getMessage());
        }
        return true;
    }

    public static boolean tryArtInstall(Context context, String dexPath,String optDexPath) {

        ZipFile lastDexZipFile = null;
        InputStream patchDexStream = null;
        ZipOutputStream zipOutputStream = null;
        byte[] buffer=new byte[1024];
        int count = 0;
        try {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            lastDexZipFile =  new ZipFile(applicationInfo.sourceDir);

            patchDexStream = new FileInputStream(dexPath);

            CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(optDexPath), new CRC32());
            zipOutputStream = new ZipOutputStream(cos);

            //先写入补丁dex
            ZipEntry patchEntry = new ZipEntry("classes.dex");
            zipOutputStream.putNextEntry(patchEntry);
            while ((count = patchDexStream.read(buffer, 0, 1024)) != -1) {
                zipOutputStream.write(buffer, 0, count);
            }
            zipOutputStream.closeEntry();

            //写入apk中的dex
            for (Enumeration<? extends ZipEntry> entries = lastDexZipFile.entries(); entries.hasMoreElements();) {
                ZipEntry apkDexEntry = (ZipEntry) entries.nextElement();
                String zipEntryName = apkDexEntry.getName();

                if(zipEntryName.startsWith(EpimetheusConstant.DEX_PREFIX) && zipEntryName.endsWith(EpimetheusConstant.DEX_SUFFIX)){

                    String index = zipEntryName.substring(EpimetheusConstant.DEX_PREFIX.length(),zipEntryName.length()-EpimetheusConstant.DEX_SUFFIX.length());
                    Integer idx = index.length() == 0 ? 1 : Integer.parseInt(index)+1;
                    String dexName = EpimetheusConstant.DEX_PREFIX + idx + EpimetheusConstant.DEX_SUFFIX;

                    InputStream orgDexInputStream = lastDexZipFile.getInputStream(apkDexEntry);

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
            return false;
        }finally {
            try {
                if (zipOutputStream !=null)
                    zipOutputStream.close();
                if (patchDexStream != null)
                    patchDexStream.close();
                if (lastDexZipFile != null){
                    lastDexZipFile.close();
                }
            }catch (Exception e){
                EpimetheusLog.e(TAG,"close the zip is fail:"+e.getMessage());
            }
        }
        return true;
    }

    public static boolean tryDalvikInstall(Context context, String dexPath,String optDexPath){
        ZipFile apk = null;
        InputStream oldDexStream = null;
        InputStream patchDexStream = null;
        OutputStream extractedDexStream = null;

        try {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            apk =  new ZipFile(applicationInfo.sourceDir);

            final String srcDexPath = "classes.dex";

            File outputFile = new File(optDexPath);

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
