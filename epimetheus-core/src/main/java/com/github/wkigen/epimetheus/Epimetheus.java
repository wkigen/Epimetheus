package com.github.wkigen.epimetheus;

import android.content.Context;
import android.content.Intent;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.loader.EpimetheusDexLoader;
import com.github.wkigen.epimetheus.loader.EpimetheusLoader;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.patch.EpimetheusPatch;
import com.github.wkigen.epimetheus.service.EpimetheusService;
import com.github.wkigen.epimetheus.utils.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sumor on 2018/4/3.
 */

public class Epimetheus {
    private final static String TAG = "Epimetheus";

    private Context context;
    private EpimetheusPatch patch;

    public Epimetheus(Context context,EpimetheusPatch patch){
        this.context = context;
        this.patch = patch;
    }

    public void installCold(){
        if (SystemUtils.isART()){
            installART();
        }else{
            installDalvik();
        }
    }

    public boolean installHot(){
        final String patchDexPath = EpimetheusManager.getEpimetheusPath()+"/"+patch.name+EpimetheusConstant.DEX_SUFFIX;
        EpimetheusLoader.tryHotInstall(context,patchDexPath,EpimetheusManager.getOptPath(),patch.fixClasses);
        return true;
    }

    private void installART(){
        try {
            final String fixZipPath = EpimetheusManager.getEpimetheusPath() + "/" +EpimetheusConstant.FIX_ZIP_NAME;
            File fixDexFile = new File(fixZipPath);
            if (fixDexFile.exists()){
                List<File> fixFiles = new ArrayList<>();
                fixFiles.add(fixDexFile);
                EpimetheusDexLoader.loadDex(context.getClassLoader(),new File(EpimetheusManager.getOptPath()),fixFiles);
            }
        }catch (Exception e){
            EpimetheusLog.e(TAG,e.getMessage());
        }
    }

    private void installDalvik() {
        try {
            final String fixDexPath = EpimetheusManager.getEpimetheusPath() + "/" + EpimetheusConstant.FIX_DEX_NAME;
            final String patchDexPath = EpimetheusManager.getEpimetheusPath() + "/" + patch.name + EpimetheusConstant.DEX_SUFFIX;
            File fixDexFile = new File(fixDexPath);
            File patchDexFile = new File(patchDexPath);
            if (fixDexFile.exists() && patchDexFile.exists()) {
                List<File> fixFiles = new ArrayList<>();
                fixFiles.add(fixDexFile);
                fixFiles.add(patchDexFile);
                EpimetheusDexLoader.loadDex(context.getClassLoader(), new File(EpimetheusManager.getOptPath()), fixFiles);
            }
        } catch (Exception e) {
            EpimetheusLog.e(TAG, e.getMessage());
        }
    }
}
