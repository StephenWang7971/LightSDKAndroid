package cn.alphabets.light.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import cn.alphabets.light.exception.NetworkException;
import cn.alphabets.light.log.Logger;
import cn.alphabets.light.network.ContextManager;
import cn.alphabets.light.network.SessionManager;
import cn.alphabets.light.setting.Default;

/**
 * 文件操作
 * Created by lin on 14/12/4.
 */
public class FileUtil {

    /**
     * 保存图片文件
     * @param bitmap 图片
     * @return 图片路径
     * @throws IOException
     */
    public static String saveBitmap(Bitmap bitmap) throws IOException {

        File file = getTemporaryFile();
        FileOutputStream stream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, Default.CompressQuality, stream);
        stream.close();

        return file.getAbsolutePath();
    }

    /**
     * 图像文件生成Bitmap实例
     * @param path 图像所在位置
     * @return Bitmap实例
     */
    public static Bitmap loadBitmap(String path) {
        try {
            return BitmapFactory.decodeStream(new FileInputStream(new File(path)));
        } catch (FileNotFoundException e) {
            Logger.e(e);
        }

        return null;
    }
    public static Bitmap loadBitmap(int resource) {
        return BitmapFactory.decodeResource(ContextManager.getInstance().getResources(), resource);
    }

    /**
     * 获取调整大小后的图像
     * @param path 图像所在位置
     * @param width 图像的宽度
     * @return
     */
    public static Bitmap loadScaledBitmap(String path, int width) {
        Bitmap bitmap = loadBitmap(path);
        if (bitmap == null) {
            return null;
        }

        int height = width * bitmap.getHeight() / bitmap.getWidth();
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    public static String scaledBitmap(String path, int width, boolean isFromCamera) {
        Bitmap bitmap = loadBitmap(path);
        // 如果是拍照获得的图片则删除原照
        if (isFromCamera) {
            File originalFile = new File(path);
            if (originalFile.isFile()) {
                originalFile.delete();
            }
        }
        if (bitmap == null) {
            return null;
        }

        float originalWidth = bitmap.getWidth();
        float originalHeight = bitmap.getHeight();
        float ratio = originalHeight / originalWidth;
        // 不管横屏竖屏，width都作为最小边
        int height = (int) (width * ratio);
        if (ratio < 1) {
            height = width;
            width = (int) (height / ratio);
        }
        if (originalHeight > width || originalWidth > width) {
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        }
        try {
            return saveBitmap(bitmap);
        } catch (IOException e) {
        }

        return null;
    }

    /**
     * 获取临时文件
     * @return 临时文件路径
     */
    public static File getTemporaryFile() {
        return new File(getCacheDir(), UUID.randomUUID().toString());
    }

    /**
     * 获取临时目录
     * @return 目录
     */
    public static File getCacheDir() {

        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED) && !Environment.isExternalStorageRemovable()) {
            return ContextManager.getInstance().getExternalCacheDir();
        }

        return ContextManager.getInstance().getCacheDir();
    }

    /**
     * 下载文件，因为是同步下载，建议使用AsyncTask
     * @param url URL
     * @param file 下载的文件
     * @throws NetworkException
     */
    public static void downloadFile(String url, File file) throws NetworkException{
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty(Default.CookieName, SessionManager.getCookie());
            urlConnection.connect();

            InputStream input = urlConnection.getInputStream();

            byte[] buffer = new byte[1024];
            int bufferLength;

            FileOutputStream output = new FileOutputStream(file);
            while ( (bufferLength = input.read(buffer)) > 0 ) {
                output.write(buffer, 0, bufferLength);
            }
            output.close();
            input.close();

        } catch (Exception e) {
            Logger.e(e);
            throw new NetworkException(e.getMessage());
        }
    }

    /**
     * 获取拍照所得图片的路径
     * @param uri
     * @param context
     * @return
     */
    public static String getPhotoPath(Uri uri, Context context) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String path = cursor.getString(1);
        cursor.close();
        return path;
    }

    /**
     * 获取图片库路径
     * @param uri url
     * @param context context
     * @return 路径
     */
    public static String getPhotoLibraryPath(Uri uri, Context context) {

        String[] projection = { MediaStore.Images.Media.DATA };

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    /**
     * 从文件里获取Mime类型
     * @param file 文件路径
     * @return mime类型
     */
    public static String getMimeTypeOfFile(String file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, options);
        return options.outMimeType;
    }

}
