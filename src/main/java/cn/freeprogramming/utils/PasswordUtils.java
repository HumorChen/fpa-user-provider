package cn.freeprogramming.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

/**
 * 密码管理器，采用pbkdf2:sha256方式生成密码
 */
public class PasswordUtils {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtils.class);

    // salt备选字符
    public static final String SALT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // salt长度
    public static final int SALT_BYTE_SIZE = 8;

    // 生成密文的长度（例：64 * 4，密文长度为64）
    public static final int HASH_BIT_SIZE = 64 * 4;

    // 迭代次数（默认迭代次数为15000）
    public static final int DEFAULT_ITERATIONS = 150000;

    // 算法名称（固定）
    public static final String algorithm = "pbkdf2:sha256";

    /**
     * 生成/获取密文
     * @param password      密码明文
     * @param salt          加盐
     * @param iterations    迭代次数
     * @return              十六进制密文
     */
    public static String getEncodedHash(String password, String salt, int iterations) {
        // Returns only the last part of whole encoded password
        SecretKeyFactory keyFactory = null;
        try {
            keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Could NOT retrieve PBKDF2WithHmacSHA256 algorithm");
            return null;
        }
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), iterations, 256);
        SecretKey secret = null;
        try {
            secret = keyFactory.generateSecret(keySpec);
        } catch (InvalidKeySpecException e) {
            logger.error("Could NOT generate secret key");
            return null;
        }

        byte[] rawHash = secret.getEncoded();
        return toHex(rawHash);
    }

    /**
     * 十六进制字符串转二进制字符串
     * @param hex   十六进制字符串
     * @return      二进制字符串
     */
    private static byte[] fromHex(String hex) {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return binary;
    }

    /**
     * 二进制字符串转十六进制字符串
     * @param array 二进制数组
     * @return      十六进制字符串
     */
    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    /**
     * 密文加盐 (获取`SALT_BYTE_SIZE`长度的盐值)
     * @return 加完盐的密文
     */
    public static String getSalt() {
        // 盐值使用ASCII表的数字加大小写字母组成
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i< SALT_BYTE_SIZE; i++) {
            sb.append(SALT_CHARS.charAt(rand.nextInt(62)));
        }
        return sb.toString();
    }

    /**
     * 获取密文
     * 默认迭代次数150000
     * @param password  明文密码
     * @return          最终指向一个密文
     */
    public static String encode(String password) {
        return encode(password, getSalt());
    }

    /**
     * 获取密文
     * @param password      明文密码
     * @param iterations    迭代次数
     * @return              最终指向一个密文
     */
    public String encode(String password, int iterations) {
        return encode(password, getSalt(), iterations);
    }

    /**
     * 获取密文
     * @param password  明文密码
     * @param salt      盐值
     * @return          最终指向一个密文
     */
    public static String encode(String password, String salt) {
        return encode(password, salt, DEFAULT_ITERATIONS);
    }

    /**
     * 最终返回的整串密文
     * 格式为算法名称+迭代次数+盐值+密文
     * {algorithm}:{iterations}${salt}${encodePassword}
     * @param password      明文密码
     * @param salt          盐值
     * @param iterations    迭代次数
     * @return              最终加密后的整串密文
     */
    public static String encode(String password, String salt, int iterations) {
        // returns hashed password, along with algorithm, number of iterations and salt
        String hash = getEncodedHash(password, salt, iterations);
        return String.format("%s:%d$%s$%s", algorithm, iterations, salt, hash);
    }

    public static String generatePasswordHash(String password, int iterations) {
        String salt = getSalt();
        return encode(password, salt, DEFAULT_ITERATIONS);
    }

    /**
     * 验证密码
     * @param password          明文
     * @param hashedPassword    密文
     * @return                  是否一致
     */
    public static boolean checkPasswordHash(String password, String hashedPassword) {
        // hashedPassword consist of: ALGORITHM, ITERATIONS_NUMBER, SALT and
        // HASH; parts are joined with dollar character ("$")
        String[] parts = hashedPassword.split("\\$");
        if (parts.length != 3) {
            // wrong hash format
            return false;
        }
        int iterations = Integer.parseInt(parts[0].split(":")[2]);
        String salt = parts[1];
        String hash = encode(password, salt, iterations);

        return hash.equals(hashedPassword);
    }
}
