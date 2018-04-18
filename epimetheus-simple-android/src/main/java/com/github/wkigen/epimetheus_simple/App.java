package com.github.wkigen.epimetheus_simple;

import android.app.Application;

import com.github.wkigen.epimetheus.EpimetheusManager;

import java.io.File;
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
        EpimetheusManager.init(this);
    }

    void init(){

        InputStream inputStream = null;
        OutputStream outputStream = null;
        final String patchName = "Patch.patch";

        try{
            inputStream =  getAssets().open(patchName);
            String path = getFilesDir().getAbsolutePath()+"/"+patchName;
            File patchFile = new File(path);
            if (patchFile.exists())
                return;
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
