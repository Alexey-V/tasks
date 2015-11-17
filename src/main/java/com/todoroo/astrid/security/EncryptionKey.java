package com.todoroo.astrid.security;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * This class handle users' or default settings of encryption.
 */

public class EncryptionKey {

    final static String folderPath = "/root/sdcard/astrid";
    final static String txtPath = folderPath + "/Encryption.txt";
    final static String modeOn = "Mode: On";
    final static String modeOff = "Mode: Off";
    final static String keyStr = "Key: ";

    protected static String keyContainer;

    public static boolean securityMode = true;


    public static boolean createSettingFile(String phoneNumber,Context context){

        // Check SD card
        String sdStatus = Environment.getExternalStorageState();

        if(!sdStatus.equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(context, "SD card not available", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check folder
        File folder = new File(folderPath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Check txt file
        File txtFile = new File (folderPath + "/Encryption.txt");

        if (!txtFile.exists()) {
            try {
                txtFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;

    }

    public static void createDefaultSetting() {

        try {
            File myFile = new File (txtPath);
            FileWriter fw = new FileWriter(myFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            bw.append(modeOn);
            bw.append(keyStr + createKey());
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String createKey() {

        // Create a random key with length of 6
        int i = ((int)((Math.random() * 9 + 1) * 100000));
        keyContainer = "" + i;

        return keyContainer;

    }

    public static void updateKey(String newPassword) {

        try {
            File file = new File(txtPath);
            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtPath), "UTF-8"));
            String str = null;

            while ((str = reader.readLine()) != null) {
                System.out.println(str);
            }

            String temp = "";
            String oldStr = keyStr + getKey();
            String replaceStr = keyStr + newPassword;

            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();

            // Keep content before this line
            for (@SuppressWarnings("unused")int i = 1; (temp = br.readLine()) != null && !temp.equals(oldStr); i++) {
                buf = buf.append(temp);
                buf = buf.append(System.getProperty("line.separator"));
            }

            buf = buf.append(replaceStr);

            // Keep content after this line
            while ((temp = br.readLine()) != null) {
                buf = buf.append(System.getProperty("line.separator"));
                buf = buf.append(temp);
            }

            br.close();
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            pw.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getKey() {

        try {
            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtPath), "UTF-8"));
            String key = null;
            int i = 0;

            while ((key = reader.readLine()) != null) {
                if (i == 1) {
                    reader.close();
                    return key.substring(4);
                }
                i++;
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public static void encryptModeOn () {

        try {
            File file = new File(txtPath);

            String temp = "";
            String oldStr = getEncryptStatus();
            String replaceStr = modeOn;

            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();

            // Keep content before this line
            for (@SuppressWarnings("unused")int i = 1; (temp = br.readLine()) != null && !temp.equals(oldStr); i++) {
                buf = buf.append(temp);
                buf = buf.append(System.getProperty("line.separator"));
            }

            buf = buf.append(replaceStr);

            // Keep content after this line
            while ((temp = br.readLine()) != null) {
                buf = buf.append(System.getProperty("line.separator"));
                buf = buf.append(temp);
            }

            br.close();
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        securityMode = true;

    }

    public static void encryptModeOff () {

        try {
            File file = new File(txtPath);

            String temp = "";
            String oldStr = getEncryptStatus();
            String replaceStr = modeOff;

            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();

            // Keep content before this line
            for (@SuppressWarnings("unused")int i = 1; (temp = br.readLine()) != null && !temp.equals(oldStr); i++) {
                buf = buf.append(temp);
                buf = buf.append(System.getProperty("line.separator"));
            }

            buf = buf.append(replaceStr);

            // Keep content after this line
            while ((temp = br.readLine()) != null) {
                buf = buf.append(System.getProperty("line.separator"));
                buf = buf.append(temp);
            }

            br.close();
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        securityMode = false;

    }

    public static String getEncryptStatus() {

        try {
            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtPath), "UTF-8"));
            String status = null;
            status = reader.readLine();
            reader.close();

            return status;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

}