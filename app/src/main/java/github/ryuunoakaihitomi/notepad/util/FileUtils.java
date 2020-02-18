package github.ryuunoakaihitomi.notepad.util;

import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class FileUtils {

    private static final String TAG = "FileUtils";

    private FileUtils() {
    }

    public static void writeTextFile(String path, String text) {
        try (FileOutputStream fos = new FileOutputStream(new File(path));
             FileChannel channel = fos.getChannel()) {
            ByteBuffer src = Charset.defaultCharset().encode(text);
            while (src.hasRemaining()) channel.write(src);
        } catch (IOException e) {
            Log.e(TAG, "writeTextFile: ", e);
        }
    }

    public static void compress(String sourcePath, String targetZipPath) {
        Log.d(TAG, "compress: start. path = " + Arrays.asList(sourcePath, targetZipPath));
        File sourceFile = new File(sourcePath);
        try (FileOutputStream fos = new FileOutputStream(targetZipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zos.setLevel(Deflater.BEST_COMPRESSION);
            compressInternal(sourceFile, sourcePath, zos);
        } catch (IOException e) {
            Log.e(TAG, "compress: ", e);
        } finally {
            long srcLen = getSizeOfDir(sourceFile);
            long tgzLen = new File(targetZipPath).length();
            Log.d(TAG, "compress: end. data compression ratio: [ " + tgzLen + " / " + srcLen + " ] = " + MathUtils.percentage(tgzLen, srcLen) + "%");
        }
    }

    private static void compressInternal(File fileToZip, String fileName, ZipOutputStream zos) throws IOException {
        if (fileToZip.isHidden()) {
            Log.w(TAG, "compressInternal: " + fileToZip + " is hidden. But it will pack into the zip file.");
        }
        if (fileToZip.isDirectory()) {
            zos.putNextEntry(new ZipEntry(fileName + (fileName.endsWith(File.separator) ? "" : File.separator)));
            zos.closeEntry();
            for (File childFile : Objects.requireNonNull(fileToZip.listFiles())) {
                compressInternal(childFile, fileName + File.separator + childFile.getName(), zos);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        zos.putNextEntry(new ZipEntry(fileName));
        byte[] bytes = new byte[1 << 10];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        fis.close();
    }

    /* https://stackoverflow.com/questions/2149785/get-size-of-folder-or-file */
    private static long getSizeOfDir(final File file) {
        long size = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                size = Files.walk(file.toPath())
                        .map(Path::toFile)
                        .filter(File::isFile)
                        .mapToLong(File::length).sum();
                return size;
            } catch (IOException e) {
                Log.e(TAG, "getSizeOfDir: use legacy situation in 26+", e);
            }
        }
        if (file.isFile()) return file.length();
        final File[] children = file.listFiles();
        if (children != null)
            for (final File child : children)
                size += getSizeOfDir(child);
        return size;
    }
}
