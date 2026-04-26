package com.app.crypto;

import com.codename1.ui.Dialog;
import com.codename1.util.Base64;
import javabc.SecureRandom;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import com.codename1.util.Base64;

public class CryptoUtils {

//    public static void main(String[] args) throws Exception {
//        String content = "4月的北京，风和日暖，春意盎然。15日上午，北京人民大会堂西大厅华灯璀璨，中国共产党党旗和越南共产党党旗并排而立，五星红旗和金星红旗相映成辉。在歌颂两国深情厚谊的《越南—中国》乐曲声中，中共中央总书记、国家主席习近平同越共中央总书记、国家主席苏林步入会场，共同会见参加“红色研学之旅”的中越青年代表。";
//        String encrypt = encrypt(content, "fasdfa");
//        String decrypt = decrypt(encrypt, "fasdfa");
//
//        // 只打印这两行，干净无乱码
//
//        System.out.println("密文:\n" + encrypt);
//        System.out.println("\n解密:\n" + decrypt);
//    }

    // ===================== 安全常量 =====================
    private static final String CHARSET = "UTF-8";
    private static final int VERSION = 1;
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 16; // 128 bit
    private static final int AES_KEY_SIZE = 256;
    private static final int PBKDF2_ITERATIONS = 65536;

    private CryptoUtils() {}

    // ===================== 加密 =====================
    public static String encrypt(String plainText, String key) throws Exception {
        if (plainText == null || plainText.isEmpty())
            throw new IllegalArgumentException("明文不能为空");
        if (key == null || key.isEmpty())
            throw new IllegalArgumentException("密钥不能为空");

        byte[] salt = generateRandomBytes(SALT_LENGTH);
        byte[] iv   = generateRandomBytes(IV_LENGTH);
        KeyParameter aesKey = generateAesKey(key, salt);

        byte[] plainBytes = plainText.getBytes(CHARSET);
        byte[] encryptedWithTag = aesGcmEncrypt(plainBytes, aesKey, iv);

        // 版本(1) + salt(16) + iv(12) + 密文(含标签)
        byte[] finalBytes = concat(
                new byte[]{(byte) VERSION},
                salt,
                iv,
                encryptedWithTag
        );
        return Base64.encode(finalBytes);
    }

    // ===================== 解密（完整修复） =====================
    public static String decrypt(String cipherText, String key) throws Exception {


        if (cipherText == null || cipherText.isEmpty())
            throw new IllegalArgumentException("密文不能为空");
        if (key == null || key.isEmpty())
            throw new IllegalArgumentException("密钥不能为空");

        byte[] all = Base64.decode(cipherText.getBytes());
        // 解析结构
        int pos = 1; // 跳过版本
        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv   = new byte[IV_LENGTH];
        System.arraycopy(all, pos, salt, 0, SALT_LENGTH); pos += SALT_LENGTH;
        System.arraycopy(all, pos, iv,   0, IV_LENGTH);   pos += IV_LENGTH;
        byte[] enc  = new byte[all.length - pos];
        System.arraycopy(all, pos, enc,  0, enc.length);

        KeyParameter aesKey = generateAesKey(key, salt);
        byte[] plainBytes = aesGcmDecrypt(enc, aesKey, iv);

        // 正确转回 UTF-8 字符串（不乱码关键）
        return new String(plainBytes, CHARSET);
    }

    // ===================== GCM 核心（修复长度与 doFinal） =====================
    private static byte[] aesGcmEncrypt(byte[] data, KeyParameter key, byte[] iv)
            throws InvalidCipherTextException {
        GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
        AEADParameters params = new AEADParameters(
                key,
                TAG_LENGTH * 8, // tag 位长
                iv,
                new byte[0]     // 第4个参数：关联数据（空）
        );
        cipher.init(true, params);

        byte[] out = new byte[cipher.getOutputSize(data.length)];
        int len = cipher.processBytes(data, 0, data.length, out, 0);
        len += cipher.doFinal(out, len); // 必须 += doFinal 返回长度

        // 只返回有效长度（不乱码关键）
        byte[] result = new byte[len];
        System.arraycopy(out, 0, result, 0, len);
        return result;
    }

    private static byte[] aesGcmDecrypt(byte[] data, KeyParameter key, byte[] iv)
            throws InvalidCipherTextException {
        GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
        AEADParameters params = new AEADParameters(
                key,
                TAG_LENGTH * 8,
                iv,
                new byte[0]
        );
        cipher.init(false, params);

        byte[] out = new byte[cipher.getOutputSize(data.length)];
        int len = cipher.processBytes(data, 0, data.length, out, 0);
        len += cipher.doFinal(out, len);

        byte[] result = new byte[len];
        System.arraycopy(out, 0, result, 0, len);
        return result;
    }

    // ===================== 工具方法 =====================
    private static KeyParameter generateAesKey(String password, byte[] salt) throws Exception {
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(password.getBytes(CHARSET), salt, PBKDF2_ITERATIONS);
        return (KeyParameter) gen.generateDerivedParameters(AES_KEY_SIZE);
    }

    private static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) total += a.length;
        byte[] res = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, res, pos, a.length);
            pos += a.length;
        }
        return res;
    }


    public static boolean isGcmCipherValid(String cipherText) {
        // 1. 空值校验
        if (cipherText == null || cipherText.length() == 0) {
            Dialog.show("提示", "密文不能为空", "确定", null);
            return false;
        }

        // 2. 纯手动剔除空格换行（不用replaceAll正则！规避封禁）
        StringBuilder cleanSb = new StringBuilder();
        for (int i = 0; i < cipherText.length(); i++) {
            char c = cipherText.charAt(i);
            // 只保留非空白字符，剔除空格、回车、换行
            if (!Character.isWhitespace(c)) {
                cleanSb.append(c);
            }
        }
        String cleanCipher = cleanSb.toString();

        // 3. CN1官方白名单Base64解码（Util类，完全合规无封禁）
        byte[] cipherBytes;
        try {
            cipherBytes = Base64.decode(cleanCipher.getBytes());
        } catch (Exception e) {
            Dialog.show("提示", "密文格式错误，非标准Base64", "确定", null);
            return false;
        }

        // 4. AES-GCM硬性长度校验：IV(12字节)+Tag(16字节)，最低28字节
        int minLen = 12 + 16;
        if (cipherBytes.length <= minLen) {
            Dialog.show("提示", "密文残缺，长度不足", "确定", null);
            return false;
        }

        // 全部校验通过，放行解密
        return true;
    }
}