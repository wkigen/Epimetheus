package com.github.wkigen.epimetheus;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.github.wkigen.epimetheus.common.EpimetheusConstant;
import com.github.wkigen.epimetheus.loader.EpimetheusDexLoader;
import com.github.wkigen.epimetheus.loader.EpimetheusLoader;
import com.github.wkigen.epimetheus.log.EpimetheusLog;
import com.github.wkigen.epimetheus.patch.Patch;
import com.github.wkigen.epimetheus.patch.PatchFixClass;
import com.github.wkigen.epimetheus.service.EpimetheusService;
import com.github.wkigen.epimetheus.utils.SystemUtils;
import com.github.wkigen.epimetheus.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Created by Dell on 2018/3/26.
 */

public class EpimetheusManager {

    public final static String TAG = "EpimetheusManager";

    public static Patch testPatch(){
        Patch patch = new Patch();
        patch.name = "Patch";

        List<String> methods = new ArrayList<>();
        methods.add("print");
        ArrayList<PatchFixClass> fixClasses = new ArrayList<>();
        fixClasses.add(new PatchFixClass("com.github.wkigen.epimetheus_simple.Patch",methods));
        patch.fixClasses = fixClasses;

        return patch;
    }

    public static void install(Application applicationp){
        Epimetheus epimetheus = new Epimetheus(applicationp,testPatch());
        epimetheus.install();
        epimetheus.checkVersion();
    }

    public static void installHot(Application applicationp){
//        Epimetheus epimetheus = new Epimetheus(applicationp,testPatch());
//        epimetheus.installHot();
//        epimetheus.checkVersion();
    }


}
