package com.github.wkigen.epimetheus.service;

import android.app.IntentService;
import android.content.Intent;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.loader.EpimetheusDexLoader;
import com.github.wkigen.epimetheus.log.EpimetheusLog;

/**
 * Created by Dell on 2018/3/26.
 */

public class
EpimetheusService extends IntentService {

    private final String TAG = "EpimetheusService";

    private final String Path = "EpimetheusService";

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
        if (patchPath == null){
            EpimetheusLog.e(TAG,"patch path can not be null");
            return;
        }

        EpimetheusDexLoader.tryColdInstall(getApplicationContext(),patchPath);

    }


}
