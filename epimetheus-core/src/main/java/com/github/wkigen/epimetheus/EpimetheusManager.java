package com.github.wkigen.epimetheus;

import android.app.Application;
import android.content.Intent;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.loader.EpimetheusDexLoader;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.service.EpimetheusService;
import com.github.wkigen.epimetheus.utils.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dell on 2018/3/26.
 */

public class EpimetheusManager {

    public final static String TAG = "EpimetheusManager";

    public static void install(Application applicationp){
        if (SystemUtils.isART()){
            installART(applicationp);
        }else{
            installDalvik(applicationp);
        }
    }

    private static void installART(Application applicationp){

        final String fixDexPath = applicationp.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH +"/"+ EpimetheusConstant.FIX_ZIP_NAME;
        final String fixDexOptPath = applicationp.getFilesDir().getAbsolutePath()+"/"+ EpimetheusConstant.FIX_DEX_OPT_PATH;
        final String patchPath = applicationp.getFilesDir().getAbsolutePath()+"/Patch.patch";
        final String dexName = "Patch.dex";

        try {
            File fixDexFile = new File(fixDexPath);
            if (fixDexFile.exists()){
                File fixPathFile = new File(fixDexOptPath);
                if (!fixPathFile.exists())
                    fixPathFile.mkdirs();
                List<File> fixFiles = new ArrayList<>();
                fixFiles.add(fixDexFile);
                EpimetheusDexLoader.loadDex(applicationp.getClassLoader(),fixPathFile,fixFiles);
            }else{
                Intent intent = new Intent(applicationp.getApplicationContext(), EpimetheusService.class);
                intent.putExtra(EpimetheusConstant.PATCH_PATH_STRING,patchPath);
                intent.putExtra(EpimetheusConstant.PATCH_DEX_STRING,dexName);
                intent.putExtra(EpimetheusConstant.PATCH_SERVICE_TYPE_STRING,EpimetheusConstant.PATCH_ART_SERVICE_TYPE_STRING);
                applicationp.getApplicationContext().startService(intent);
            }
        }catch (Exception e){
            EpimetheusLog.e(TAG,e.getMessage());
        }
    }

    private static void installDalvik(Application applicationp){

        final String fixDexPath = applicationp.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH +"/"+ EpimetheusConstant.FIX_DEX_NAME;
        final String fixDexOptPath = applicationp.getFilesDir().getAbsolutePath()+"/"+ EpimetheusConstant.FIX_DEX_OPT_PATH;
        final String patchPath = applicationp.getFilesDir().getAbsolutePath()+"/Patch.patch";
        final String dexName = "Patch.dex";
        final String patchDexPath = applicationp.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH+"/"+dexName;

        try {
            File fixDexFile = new File(fixDexPath);
            File patchDexFile = new File(patchDexPath);
            if (fixDexFile.exists() && patchDexFile.exists()){
                File fixPathFile = new File(fixDexOptPath);
                if (!fixPathFile.exists())
                    fixPathFile.mkdirs();

                List<File> fixFiles = new ArrayList<>();
                fixFiles.add(fixDexFile);
                fixFiles.add(patchDexFile);
                EpimetheusDexLoader.loadDex(applicationp.getClassLoader(),fixPathFile,fixFiles);
            }else{
                Intent intent = new Intent(applicationp.getApplicationContext(), EpimetheusService.class);
                intent.putExtra(EpimetheusConstant.PATCH_PATH_STRING,patchPath);
                intent.putExtra(EpimetheusConstant.PATCH_DEX_STRING,dexName);
                intent.putExtra(EpimetheusConstant.PATCH_SERVICE_TYPE_STRING,EpimetheusConstant.PATCH_DALUIK_SERVICE_TYPE_STRING);
                applicationp.getApplicationContext().startService(intent);
            }
        }catch (Exception e){
            EpimetheusLog.e(TAG,e.getMessage());
        }

    }





}
