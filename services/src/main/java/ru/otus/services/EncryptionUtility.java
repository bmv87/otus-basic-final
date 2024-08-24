package ru.otus.services;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public class EncryptionUtility {

    public static final String MESSAGE_DIGEST_ALGORITHM = "MD5";
    public static final String TRANSFORMATION = "AES";
    public static final String SECRET_KEY_ALGORITHM = "AES";
    public static final byte[] SALT = "123456789987654321".getBytes(StandardCharsets.UTF_8);

    public static String encrypt(String sourceStr) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKeySpec key = createSecretKey();
        return encrypt(sourceStr, key);
    }

    private static SecretKeySpec createSecretKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        MessageDigest messageDigest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
        byte[] key = messageDigest.digest(SALT);
        key = Arrays.copyOf(key, 16); // use only first 128 bit

        return new SecretKeySpec(key, SECRET_KEY_ALGORITHM);
    }

    private static String encrypt(String property, SecretKeySpec key) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher pbeCipher = Cipher.getInstance(TRANSFORMATION);
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cryptoText = pbeCipher.doFinal(property.getBytes(StandardCharsets.UTF_8));
        return base64Encode(cryptoText);
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String decrypt(String string) throws GeneralSecurityException, IOException {
        SecretKeySpec key = createSecretKey();
        Cipher pbeCipher = Cipher.getInstance(TRANSFORMATION);
        pbeCipher.init(Cipher.DECRYPT_MODE, key);
        return new String(pbeCipher.doFinal(base64Decode(string)), StandardCharsets.UTF_8);
    }

    private static byte[] base64Decode(String property) throws IOException {
        return Base64.getDecoder().decode(property);
    }
}
