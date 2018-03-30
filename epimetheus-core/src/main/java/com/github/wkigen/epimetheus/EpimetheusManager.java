package com.github.wkigen.epimetheus;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.loader.EpimetheusDexLoader;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.service.EpimetheusService;
import com.github.wkigen.epimetheus.utils.ReflectUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by Dell on 2018/3/26.
 */

public class EpimetheusManager {

    public final static String TAG = "EpimetheusManager";


    public static void installDalvik(Application applicationp){

        final String fixDexPath = applicationp.getFilesDir().getAbsolutePath()+"/" + EpimetheusConstant.FIX_DEX_NAME;
        final String fixDexOptPath = applicationp.getFilesDir().getAbsolutePath()+"/"+ EpimetheusConstant.FIX_DEX_PATH;
        final String patchPath = applicationp.getFilesDir().getAbsolutePath()+"/Patch.patch";

        try {
            File fixDexFile = new File(fixDexPath);
            File patchFile = new File(patchPath);
            if (fixDexFile.exists() && patchFile.exists()){
                File fixPathFile = new File(fixDexOptPath);

                List<File> fixFiles = new ArrayList<>();
                //fixFiles.add(fixDexFile);
                fixFiles.add(patchFile);
                EpimetheusDexLoader.loadFixDalvikDex(applicationp.getClassLoader(),fixPathFile,fixFiles);
            }else{
                //cold install
                Intent intent = new Intent(applicationp.getApplicationContext(), EpimetheusService.class);
                intent.putExtra(EpimetheusConstant.PATCH_PATH_STRING,patchPath);
                applicationp.getApplicationContext().startService(intent);
            }
        }catch (Exception e){
            EpimetheusLog.e(TAG,e.getMessage());
        }

    }





}
