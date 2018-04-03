package com.github.wkigen.epimetheus.patch;

import com.github.wkigen.epimetheus.dex.ClassDef;
import com.github.wkigen.epimetheus.dex.Dex;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Dell on 2018/3/27.
 */

public class Patch {

    public String name;
    public List<PatchFixClass> fixClasses = new ArrayList<>();

    public Patch(){

    }


}
