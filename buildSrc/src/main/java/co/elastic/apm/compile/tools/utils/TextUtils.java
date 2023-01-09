package co.elastic.apm.compile.tools.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TextUtils {

    public static void writeText(File outputFile, String text) {
        try (OutputStream stream = new FileOutputStream(outputFile)) {
            writeText(stream, text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String hashMd5(String input) {
        try {
            MessageDigest md5Hasher = MessageDigest.getInstance("MD5");
            md5Hasher.update(input.getBytes(StandardCharsets.UTF_8));
            return toHex(md5Hasher.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    public static void writeText(OutputStream stream, String text) {
        try {
            stream.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addSeparator(OutputStream out, Character character) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n\n");
        builder.append(String.valueOf(character).repeat(79));
        builder.append("\n\n");
        TextUtils.writeText(out, builder.toString());
    }
}
