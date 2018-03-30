package com.github.wkigen.epimetheus.patch;

import com.github.wkigen.epimetheus.dex.ClassDef;
import com.github.wkigen.epimetheus.dex.Dex;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Dell on 2018/3/27.
 */

public class Patch {



    public static boolean deleteClass(byte[] src, byte[] patch, OutputStream out){

        try{

            Dex oldDex = new Dex(src);
            Dex patchDex = new Dex(patch);

            Set<Integer> deleteClassSet = new HashSet<>();
            for (ClassDef patchClassDef : patchDex.classDefs()){
                String patchClassName = patchDex.typeNames().get(patchClassDef.getTypeIndex());
                for (ClassDef oldClassDef : oldDex.classDefs()){
                    String oldClassName = oldDex.typeNames().get(oldClassDef.getTypeIndex());
                    if (patchClassName.equals(oldClassName)){
                        deleteClassSet.add(oldClassDef.getTypeIndex());
                    }
                }
            }

        }catch (Exception e){
            return false;
        }

        return true;
    }

}
