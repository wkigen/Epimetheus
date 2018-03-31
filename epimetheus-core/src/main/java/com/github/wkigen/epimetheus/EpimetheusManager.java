package com.github.wkigen.epimetheus;

import android.app.Application;
import android.content.Intent;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.loader.EpimetheusDexLoader;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.service.EpimetheusService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dell on 2018/3/26.
 */

public class EpimetheusManager {

    public final static String TAG = "EpimetheusManager";


    public static void installDalvik(Application applicationp){

        final String fixDexPath = applicationp.getFilesDir().getAbsolutePath()+"/" + EpimetheusConstant.FIX_DEX_NAME;
        final String fixDexOptPath = applicationp.getFilesDir().getAbsolutePath()+"/"+ EpimetheusConstant.FIX_DEX_OPT_PATH;
        final String patchPath = applicationp.getFilesDir().getAbsolutePath()+"/Patch.patch";
        final String dexName = "Patch.dex";
        final String patchDexPath = applicationp.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH+"/"+dexName;


        try {
            File fixDexFile = new File(fixDexPath);
            File patchDexFile = new File(patchDexPath);
            if (fixDexFile.exists() && patchDexFile.exists()){
                File fixPathFile = new File(fixDexOptPath);

                List<File> fixFiles = new ArrayList<>();
                //fixFiles.add(fixDexFile);
                fixFiles.add(patchDexFile);
                EpimetheusDexLoader.loadFixDalvikDex(applicationp.getClassLoader(),fixPathFile,fixFiles);
            }else{
                //cold install
                Intent intent = new Intent(applicationp.getApplicationContext(), EpimetheusService.class);
                intent.putExtra(EpimetheusConstant.PATCH_PATH_STRING,patchPath);
                intent.putExtra(EpimetheusConstant.PATCH_DEX_STRING,dexName);
                applicationp.getApplicationContext().startService(intent);
            }
        }catch (Exception e){
            EpimetheusLog.e(TAG,e.getMessage());
        }

    }





}
