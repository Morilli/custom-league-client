package com.hawolt.client.misc;

import com.hawolt.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Created: 28/08/2023 18:31
 * Author: Twitter @hawolt
 **/

public class Base64RawInflate {

    public static String encode(byte[] b) {
        return Base64.getEncoder().encodeToString(b);
    }

    public static byte[] decode(String s) {
        return Base64.getDecoder().decode(s);
    }

    public static String inflate(byte[] b) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(b.length);
        Inflater decompressor = new Inflater(true);
        try {
            decompressor.setInput(b);
            final byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                baos.write(buf, 0, count);
            }
        } catch (Exception e) {
            Logger.error(e);
        } finally {
            decompressor.end();
        }
        return baos.toString();
    }

    public static byte[] deflate(byte[] b) {
        Deflater compressor = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        int size = 0;
        final byte[] buffer = new byte[10240];
        try {
            compressor.setInput(b);
            compressor.finish();
            size = compressor.deflate(buffer);
        } catch (Exception e) {
            Logger.error(e);
        } finally {
            compressor.end();
        }
        byte[] result = new byte[size];
        System.arraycopy(buffer, 0, result, 0, size);
        return result;
    }
}
