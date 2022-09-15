package co.elastic.apm.compile.tools.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TextUtils {

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
        builder.append(String.valueOf(character).repeat(50));
        builder.append("\n\n");
        TextUtils.writeText(out, builder.toString());
    }
}
