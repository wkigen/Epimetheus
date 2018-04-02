package com.github.wkigen.epimetheus.service;

import android.app.IntentService;
import android.content.Intent;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.loader.EpimetheusLoader;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.utils.Utils;

/**
 * Created by Dell on 2018/3/26.
 */

public class
EpimetheusService extends IntentService {

    private final String TAG = "EpimetheusService";

    public EpimetheusService() {
        super(EpimetheusService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null){
            EpimetheusLog.e(TAG,"inten can not be null");
            return;
        }

        String patchPath = intent.getStringExtra(EpimetheusConstant.PATCH_PATH_STRING);
        String epimetheusPath = getFilesDir().getAbsolutePath()+"/"+EpimetheusConstant.EPIMETHEUS_PATH;
        String patchDexName = intent.getStringExtra(EpimetheusConstant.PATCH_DEX_STRING);
        String serviceType = intent.getStringExtra(EpimetheusConstant.PATCH_SERVICE_TYPE_STRING);

        if (patchPath == null || patchDexName == null){
            EpimetheusLog.e(TAG,"patch path or dex name can not be null");
            return;
        }

        if (serviceType.equals(EpimetheusConstant.PATCH_ART_SERVICE_TYPE_STRING))
             EpimetheusLoader.tryArtInstall(getApplicationContext(),epimetheusPath+"/"+patchDexName);
        else
            EpimetheusLoader.tryDalvikInstall(getApplicationContext(),epimetheusPath+"/"+patchDexName);
    }


}
