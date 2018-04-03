package com.github.wkigen.epimetheus;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.loader.EpimetheusDexLoader;
import com.github.wkigen.epimetheus.loader.EpimetheusLoader;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.patch.Patch;
import com.github.wkigen.epimetheus.service.EpimetheusService;
import com.github.wkigen.epimetheus.utils.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sumor on 2018/4/3.
 */

public class Epimetheus {
    public final static String TAG = "Epimetheus";

    private Context context;
    private String epimetheusPath;
    private String optPath;
    private Patch patch;

    public Epimetheus(Context context,Patch patch){
        this.context = context;
        this.patch = patch;

        epimetheusPath = context.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH;
        optPath = epimetheusPath +"/"+ EpimetheusConstant.FIX_DEX_OPT_PATH;

        File epimtheusFile = new File(epimetheusPath);
        File optFile = new File(optPath);
        if (!epimtheusFile.exists())
            epimtheusFile.exists();
        if (!optFile.exists())
            optFile.mkdirs();
    }

    //冷启动
    public void install(){
        if (SystemUtils.isART()){
            installART();
        }else{
            installDalvik();
        }
    }

    public void checkVersion(){

    }

    public boolean installHot(){
        final String patchDexPath = epimetheusPath+"/"+patch.name+EpimetheusConstant.DEX_SUFFIX;
        EpimetheusLoader.tryHotInstall(context,patchDexPath,optPath,patch.fixClasses);
        return true;
    }

    private void installART(){
        try {
            final String fixZipPath = epimetheusPath + "/" +EpimetheusConstant.FIX_ZIP_NAME;
            final String patchDexPath = epimetheusPath+"/"+patch.name+EpimetheusConstant.DEX_SUFFIX;
            File fixDexFile = new File(fixZipPath);
            if (fixDexFile.exists()){
                List<File> fixFiles = new ArrayList<>();
                fixFiles.add(fixDexFile);
                EpimetheusDexLoader.loadDex(context.getClassLoader(),new File(optPath),fixFiles);
            }else{
                Intent intent = new Intent(context, EpimetheusService.class);
                intent.putExtra(EpimetheusConstant.PATCH_DEX_STRING,patchDexPath);
                intent.putExtra(EpimetheusConstant.PATCH_OPT_DEX_STRING,fixZipPath);
                intent.putExtra(EpimetheusConstant.PATCH_SERVICE_TYPE_STRING,EpimetheusConstant.PATCH_ART_SERVICE_TYPE_STRING);
                context.startService(intent);
            }
        }catch (Exception e){
            EpimetheusLog.e(TAG,e.getMessage());
        }
    }

    private void installDalvik() {

        final String fixDexPath = epimetheusPath + "/" + EpimetheusConstant.FIX_DEX_NAME;
        final String patchDexPath = epimetheusPath + "/" + patch.name + EpimetheusConstant.DEX_SUFFIX;

        try {
            File fixDexFile = new File(fixDexPath);
            File patchDexFile = new File(patchDexPath);
            if (fixDexFile.exists() && patchDexFile.exists()) {
                List<File> fixFiles = new ArrayList<>();
                fixFiles.add(fixDexFile);
                fixFiles.add(patchDexFile);
                EpimetheusDexLoader.loadDex(context.getClassLoader(), new File(optPath), fixFiles);
            } else {
                Intent intent = new Intent(context.getApplicationContext(), EpimetheusService.class);
                intent.putExtra(EpimetheusConstant.PATCH_DEX_STRING,patchDexPath);
                intent.putExtra(EpimetheusConstant.PATCH_OPT_DEX_STRING,fixDexPath);
                intent.putExtra(EpimetheusConstant.PATCH_SERVICE_TYPE_STRING,EpimetheusConstant.PATCH_DALUIK_SERVICE_TYPE_STRING);
                context.startService(intent);
            }
        } catch (Exception e) {
            EpimetheusLog.e(TAG, e.getMessage());
        }
    }
}
