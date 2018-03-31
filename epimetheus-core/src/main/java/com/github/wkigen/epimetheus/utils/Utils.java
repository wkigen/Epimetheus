package com.github.wkigen.epimetheus.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    public static void unZipPatch(String patch,String unZipPath){

        ZipFile zipFile = null;
        File pathFile = null;
        try{
            zipFile =  new ZipFile(new File(patch));
            pathFile = new File(unZipPath);
            if (!pathFile.exists())
                pathFile.mkdirs();

            for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                InputStream in = zipFile.getInputStream(entry);
                String outPath = (unZipPath+ "/" + zipEntryName).replaceAll("\\*", "/");

                File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                if (!file.exists()) {
                    file.mkdirs();
                }

                if (new File(outPath).isDirectory()) {
                    continue;
                }

                FileOutputStream  out = new FileOutputStream(outPath);
                byte[] buf1 = new byte[1024];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
                in.close();
                out.close();
            }
        }catch (Exception e){

        }finally {
            try{
                if (zipFile != null)
                    zipFile.close();
            }catch (Exception e){

            }
        }
    }

}
