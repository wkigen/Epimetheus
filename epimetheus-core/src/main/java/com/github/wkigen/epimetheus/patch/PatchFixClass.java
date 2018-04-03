package com.github.wkigen.epimetheus.patch;

import java.util.List;

/**
 * Created by sumor on 2018/4/3.
 */

public class PatchFixClass {
    public String name;
    public List<String> fixMethod;

    public PatchFixClass(String name,List<String> fixMethod){
        this.name = name;
        this.fixMethod = fixMethod;
    }
}
