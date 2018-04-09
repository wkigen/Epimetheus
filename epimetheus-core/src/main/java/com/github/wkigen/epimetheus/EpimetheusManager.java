package com.github.wkigen.epimetheus;

import android.app.Application;
import android.content.Intent;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.patch.EpimetheusPatch;
import com.github.wkigen.epimetheus.service.EpimetheusService;
import com.github.wkigen.epimetheus.utils.SystemUtils;
import com.github.wkigen.epimetheus.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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

        final String patchInfoPath = epimetheusPath+"/"+ EpimetheusConstant.PATCH_INFO_FILE_NAME;
        File patchInfoFile = new File(patchInfoPath);
        if (patchInfoFile.exists()){
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(patchInfoFile);
                EpimetheusPatch patch = new EpimetheusPatch();
                patch.read(inputStream);
                Epimetheus epimetheus = new Epimetheus(application,patch);
                epimetheus.installCold();
            }catch (Exception e){
            }finally {
                try{
                    if (inputStream != null)
                        inputStream.close();
                }catch (Exception e){
                }
            }
        }
    }

    public static void installPatch(String path){
        EpimetheusPatch patch = Utils.unZipPatch(path,application.getFilesDir().getAbsolutePath()+"/"+ EpimetheusConstant.EPIMETHEUS_PATH);
        if (patch != null){
            if (patch.canHot){
                Epimetheus epimetheus = new Epimetheus(application,patch);
                epimetheus.installHot();
            }
            final String fixDexPath = EpimetheusManager.getEpimetheusPath() + "/" + (SystemUtils.isART()?EpimetheusConstant.FIX_ZIP_NAME :EpimetheusConstant.FIX_DEX_NAME  );
            final String patchDexPath = EpimetheusManager.getEpimetheusPath() + "/" + patch.name + EpimetheusConstant.DEX_SUFFIX;
            Intent intent = new Intent(application.getApplicationContext(), EpimetheusService.class);
            intent.putExtra(EpimetheusConstant.PATCH_DEX_STRING,patchDexPath);
            intent.putExtra(EpimetheusConstant.PATCH_OPT_DEX_STRING,fixDexPath);
            application.startService(intent);
        }
    }
}
