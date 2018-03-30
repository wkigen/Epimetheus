package com.github.wkigen.epimetheus.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by Dell on 2018/3/27.
 */

public class Utils {

    public static byte[] readByte(InputStream inputStream){
        ByteArrayOutputStream outStream = null;
        try{
            outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while((len = inputStream.read(buffer)) != -1){
                outStream.write(buffer, 0, len);
            }
        }catch (Exception e){
            return null;
        }finally {
            try{
                if (outStream != null)
                    outStream.close();
            }catch (Exception e){

            }
        }

        return outStream.toByteArray();
    }

}
