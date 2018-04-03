package com.github.wkigen.epimetheus.utils;

/**
 * Created by Dell on 2018/3/26.
 */

public class SystemUtils {

    public static boolean isART(){
        String version = System.getProperty("java.vm.version");
        if (version != null){
            String[] temp = version.split("\\.");
            if (temp.length > 2 ){
                int major = Integer.parseInt(temp[0]);
                int minor = Integer.parseInt(temp[1]);
                if (major >= 2 ){
                    return true;
                }
            }
        }
        return false;
    }

}
