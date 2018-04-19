package com.github.wkigen.epimetheus.patch;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dell on 2018/3/27.
 */

public class EpimetheusPatch {

    private final static String XML_PATCH = "patch";
    private final static String XML_PATCHNAME = "patch_name";
    private final static String XML_VERSION = "version";
    private final static String XML_CANHOT = "can_hot";
    private final static String XML_CLASS = "class";
    private final static String XML_CLASS_NAME = "class_name";
    private final static String XML_METHOD_NAME = "method_name";
    private final static String XML_METHOD_PARAM = "method_param";


    public String name;
    public String version;
    public boolean canHot = false;
    public List<FixClassInfo> fixClasses = new ArrayList<>();

    public EpimetheusPatch(){

    }

    public boolean read(InputStream inputStream){
        try{
            XmlPullParser xmlPullParser= Xml.newPullParser();
            xmlPullParser.setInput(inputStream,"UTF-8");

            //获取解析的标签的类型
            int type=xmlPullParser.getEventType();
            FixClassInfo fixClassInfo = null;
            FixMethodInfo fixMethodInfo = null;
            while(type!=XmlPullParser.END_DOCUMENT){
                switch (type) {
                    case XmlPullParser.START_TAG:
                        //获取开始标签的名字
                        String starttgname = xmlPullParser.getName();
                        switch (starttgname){
                            case XML_VERSION:
                                version = xmlPullParser.nextText();
                                break;
                            case XML_PATCHNAME:
                                name = xmlPullParser.nextText();
                                break;
                            case XML_CLASS_NAME:
                                fixClassInfo = new FixClassInfo(xmlPullParser.nextText());
                                break;
                            case XML_METHOD_NAME:
                                fixMethodInfo = new FixMethodInfo(xmlPullParser.nextText());
                                if (fixClassInfo != null)
                                    fixClassInfo.methods.add(fixMethodInfo);
                                break;
                            case XML_METHOD_PARAM:
                                if (fixMethodInfo != null)
                                    fixMethodInfo.params.add(xmlPullParser.nextText());
                                break;
                            case XML_CANHOT:
                                String temp = xmlPullParser.nextText();
                                if ("true".equals(temp)){
                                    canHot = true;
                                }
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        String endgname = xmlPullParser.getName();
                        switch (endgname){
                            case XML_CLASS:
                                if (fixClassInfo != null)
                                    fixClasses.add(fixClassInfo);
                                break;
                        }
                        break;
                }
                type=xmlPullParser.next();
            }
        }catch (Exception e){
            return false;
        }

        return true;
    }

    public static final class FixClassInfo {
        public String name;
        public List<FixMethodInfo> methods = new ArrayList<>();

        public FixClassInfo(String name){
            this.name = name;
        }
    }

    public static final class FixMethodInfo{
        public String name;
        public List<String> params = new ArrayList<>();
        public FixMethodInfo(String name){
            this.name = name;
        }
    }

}
