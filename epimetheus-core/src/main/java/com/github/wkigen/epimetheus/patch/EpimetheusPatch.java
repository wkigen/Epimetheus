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

    public String name;
    public String version;
    public boolean canHot = false;
    public List<String> fixClasses = new ArrayList<>();

    public EpimetheusPatch(){

    }

    public boolean read(InputStream inputStream){
        try{
            XmlPullParser xmlPullParser= Xml.newPullParser();
            xmlPullParser.setInput(inputStream,"UTF-8");

            //获取解析的标签的类型
            int type=xmlPullParser.getEventType();
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
                            case XML_CLASS:
                                fixClasses.add(xmlPullParser.nextText());
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
                        break;
                }
                type=xmlPullParser.next();
            }
        }catch (Exception e){
            return false;
        }

        return true;
    }

}
