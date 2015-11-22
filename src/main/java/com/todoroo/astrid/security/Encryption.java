package com.todoroo.astrid.security;

/**
 * This class handle basic AES encryption functions for encrypting backup files.
 * Only strings of a task will be encrypted.
 * @author Jiayu
 */

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class Encryption {

    private static SecretKeySpec secretKey ;
    protected static byte[] key ;

    private static String encryptedStr;
    private static String decryptedStr;


    // Create the secret key
    public static void setKey(String str) {
        MessageDigest sha;
        String myKey = str;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // use only first 128 bit
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            setEncryptedStr(Base64.encode(cipher.doFinal(strToEncrypt.getBytes("UTF-8"))));
        } catch (Exception e) {
            System.err.println("Error while encrypting: " + e.toString());
        }
    }

    public static void decrypt(String strToDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            setDecryptedStr(new String(cipher.doFinal(Base64.decode(strToDecrypt))));
        } catch (Exception e) {
            System.err.println("Error while decrypting: " + e.toString());
        }
    }

    public static void setEncryptedStr(String encryptedStr) {
        Encryption.encryptedStr = encryptedStr;
    }

    public static String getEncryptedStr() {
        return encryptedStr;
    }

    public static void setDecryptedStr(String decryptedString) {
        Encryption.decryptedStr = decryptedString;
    }

    public static String getDecryptedString() {
        return decryptedStr;
    }

}