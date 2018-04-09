package com.github.wkigen.epimetheus;

import android.app.Application;
import android.content.Intent;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.patch.EpimetheusPatch;
import com.github.wkigen.epimetheus.service.EpimetheusService;
import com.github.wkigen.epimetheus.utils.SystemUtils;
import com.github.wkigen.epimetheus.utils.Utils;

import java.io.File;

/**
 * Created by Dell on 2018/3/26.
 */

public class EpimetheusManager {

    private final static String TAG = "EpimetheusManager";

    private static String epimetheusPath;
    private static String optPath;
    private static Application application;
    public static String getEpimetheusPath() {
        return epimetheusPath;
    }

    public static String getOptPath() {
        return optPath;
    }

    public static EpimetheusPatch testPatch(){
        EpimetheusPatch patch = new EpimetheusPatch();
        patch.name = "EpimetheusPatch";
        patch.fixClasses.add("com.github.wkigen.epimetheus_simple.EpimetheusPatch");
        return patch;
    }

    public static void init(Application app){
        application = app;

        epimetheusPath = application.getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH;
        optPath = epimetheusPath +"/"+ EpimetheusConstant.FIX_DEX_OPT_PATH;

        File epimtheusFile = new File(epimetheusPath);
        File optFile = new File(optPath);
        if (!epimtheusFile.exists())
            epimtheusFile.exists();
        if (!optFile.exists())
            optFile.mkdirs();
    }

    public static void installPatch(String path){
        EpimetheusPatch patch = Utils.unZipPatch(path,application.getFilesDir().getAbsolutePath()+"/"+ EpimetheusConstant.EPIMETHEUS_PATH);
        if (patch != null){
            if (patch.canHot){
                installHot(patch);
            }
            final String fixDexPath = EpimetheusManager.getEpimetheusPath() + "/" + (SystemUtils.isART()?EpimetheusConstant.FIX_ZIP_NAME :EpimetheusConstant.FIX_DEX_NAME  );
            final String patchDexPath = EpimetheusManager.getEpimetheusPath() + "/" + patch.name + EpimetheusConstant.DEX_SUFFIX;
            Intent intent = new Intent(application.getApplicationContext(), EpimetheusService.class);
            intent.putExtra(EpimetheusConstant.PATCH_DEX_STRING,patchDexPath);
            intent.putExtra(EpimetheusConstant.PATCH_OPT_DEX_STRING,fixDexPath);
            application.startService(intent);
        }
    }

    public static void installCold(EpimetheusPatch patch){
        Epimetheus epimetheus = new Epimetheus(application,testPatch());
        epimetheus.installCold();
    }

    public static void installHot(EpimetheusPatch patch){
        Epimetheus epimetheus = new Epimetheus(application,patch);
        epimetheus.installHot();
    }


}
