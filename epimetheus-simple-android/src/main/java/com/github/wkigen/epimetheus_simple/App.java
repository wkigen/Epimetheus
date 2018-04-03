package com.github.wkigen.epimetheus_simple;

import android.app.Application;

import com.github.wkigen.epimetheus.EpimetheusManager;
import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.jni.EpimetheusJni;
import com.github.wkigen.epimetheus.utils.Utils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Dell on 2018/3/27.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        init();

        Utils.unZipPatch(getFilesDir().getAbsolutePath()+"/Patch.patch",getFilesDir().getAbsolutePath()+"/"+ EpimetheusConstant.EPIMETHEUS_PATH);

        EpimetheusManager.install(this);
    }

    void init(){

        InputStream inputStream = null;
        OutputStream outputStream = null;
        final String patchName = "Patch.patch";

        try{
            inputStream =  getAssets().open(patchName);
            String path = getFilesDir().getAbsolutePath()+"/"+patchName;
            outputStream = new FileOutputStream(path);

            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer,0,1024);
            while (len > 0){
                outputStream.write(buffer,0,1024);
                len = inputStream.read(buffer,0,1024);
            }
        }catch (Exception E){

        } finally {
            try{
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null){
                    outputStream.close();
                }
            }catch (Exception e){

            }
        }

    }


}
