package com.github.wkigen.epimetheus;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.loader.EpimetheusDexLoader;
import com.github.wkigen.epimetheus.loader.EpimetheusLoader;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.service.EpimetheusService;
import com.github.wkigen.epimetheus.utils.SystemUtils;
import com.github.wkigen.epimetheus.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Created by Dell on 2018/3/26.
 */

public class EpimetheusManager {

    public final static String TAG = "EpimetheusManager";

    public static void install(Application applicationp){

        //init
        final String dexName = "Patch.dex";
        final String patchPath = applicationp.getFilesDir().getAbsolutePath()+"/Patch.patch";
        final String fixDexOptPath = applicationp.getFilesDir().getAbsolutePath()+"/"+ EpimetheusConstant.FIX_DEX_OPT_PATH;
        String unZipPath = applicationp.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH;

        File fixPathFile = new File(fixDexOptPath);
        if (!fixPathFile.exists()){
            fixPathFile.mkdirs();
            Utils.unZipPatch(patchPath,unZipPath);
        }

        installHot(applicationp,fixDexOptPath,dexName);

//        if (SystemUtils.isART()){
//            installART(applicationp,fixPathFile,patchPath,dexName);
//        }else{
//            installDalvik(applicationp,fixPathFile,patchPath,dexName);
//        }
    }

    public static boolean installHot(Application applicationp,String fixDexOptPath,String patchDexName){

        final String patchClassName = "com.github.wkigen.epimetheus_simple.Patch";
        final String patchMethodName = "print";

        final String patchDexPath = applicationp.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH+"/"+patchDexName;

        EpimetheusLoader.tryHotInstall(applicationp,patchDexPath,fixDexOptPath,patchClassName,patchMethodName);

        return true;
    }

    private static void installART(Application applicationp,File fixPathFile,String patchPath,String patchDexName){

        final String fixDexPath = applicationp.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH +"/"+ EpimetheusConstant.FIX_ZIP_NAME;

        try {
            File fixDexFile = new File(fixDexPath);
            if (fixDexFile.exists()){
                List<File> fixFiles = new ArrayList<>();
                fixFiles.add(fixDexFile);
                EpimetheusDexLoader.loadDex(applicationp.getClassLoader(),fixPathFile,fixFiles);
            }else{
                Intent intent = new Intent(applicationp.getApplicationContext(), EpimetheusService.class);
                intent.putExtra(EpimetheusConstant.PATCH_PATH_STRING,patchPath);
                intent.putExtra(EpimetheusConstant.PATCH_DEX_STRING,patchDexName);
                intent.putExtra(EpimetheusConstant.PATCH_SERVICE_TYPE_STRING,EpimetheusConstant.PATCH_ART_SERVICE_TYPE_STRING);
                applicationp.getApplicationContext().startService(intent);
            }
        }catch (Exception e){
            EpimetheusLog.e(TAG,e.getMessage());
        }
    }

    private static void installDalvik(Application applicationp,File fixPathFile,String patchPath,String patchDexName){

        final String fixDexPath = applicationp.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH +"/"+ EpimetheusConstant.FIX_DEX_NAME;
        final String patchDexPath = applicationp.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH+"/"+patchDexName;

        try {
            File fixDexFile = new File(fixDexPath);
            File patchDexFile = new File(patchDexPath);
            if (fixDexFile.exists() && patchDexFile.exists()){
                List<File> fixFiles = new ArrayList<>();
                fixFiles.add(fixDexFile);
                fixFiles.add(patchDexFile);
                EpimetheusDexLoader.loadDex(applicationp.getClassLoader(),fixPathFile,fixFiles);
            }else{
                Intent intent = new Intent(applicationp.getApplicationContext(), EpimetheusService.class);
                intent.putExtra(EpimetheusConstant.PATCH_PATH_STRING,patchPath);
                intent.putExtra(EpimetheusConstant.PATCH_DEX_STRING,patchDexName);
                intent.putExtra(EpimetheusConstant.PATCH_SERVICE_TYPE_STRING,EpimetheusConstant.PATCH_DALUIK_SERVICE_TYPE_STRING);
                applicationp.getApplicationContext().startService(intent);
            }
        }catch (Exception e){
            EpimetheusLog.e(TAG,e.getMessage());
        }

    }





}
