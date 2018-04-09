package com.github.wkigen.epimetheus.service;

import android.app.IntentService;
import android.content.Intent;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.loader.EpimetheusLoader;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.utils.SystemUtils;
import com.github.wkigen.epimetheus.utils.Utils;

/**
 * Created by Dell on 2018/3/26.
 */

public class EpimetheusService extends IntentService {

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

        String patchDexPath = intent.getStringExtra(EpimetheusConstant.PATCH_DEX_STRING);
        String optDexPath = intent.getStringExtra(EpimetheusConstant.PATCH_OPT_DEX_STRING);

        if (patchDexPath== null || optDexPath == null){
            EpimetheusLog.e(TAG,"patchDexPath or optDexPath can not be null");
            return;
        }

        if (SystemUtils.isART())
             EpimetheusLoader.tryArtInstall(getApplicationContext(),patchDexPath,optDexPath);
        else
            EpimetheusLoader.tryDalvikInstall(getApplicationContext(),patchDexPath,optDexPath);
    }


}
