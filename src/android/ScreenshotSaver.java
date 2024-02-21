package com.darktalker.cordova.screenshot;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.annotation.TargetApi;

@TargetApi(24)
public class ScreenshotSaver {
    CordovaInterface cordova;
    View view;
    String fileName;
    CallbackContext pluginContext;
    private final String TAG = "ScreenshotSaver";

    public ScreenshotSaver(CordovaInterface cordova, View view, String fileName, CallbackContext pluginContext) {
        this.cordova = cordova;
        this.view = view;
        this.fileName = fileName;
        this.pluginContext = pluginContext;
    }

    public void saveScreenshot(Bitmap bitmap) {
        try {
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Context context = cordova.getContext();
            ContentResolver resolver = context.getContentResolver();

            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            try {
                OutputStream fos = resolver.openOutputStream(imageUri);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                JSONObject jsonRes = new JSONObject();
                jsonRes.put("filePath", imageUri);
                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonRes);
                pluginContext.sendPluginResult(result);
                fos.close();
            } catch (Exception e) {
                pluginContext.error(e.getMessage());
            }

            // } else {
            // File f = new
            // File(cordova.getActivity().getApplicationContext().getFilesDir(),
            // fileName + ".jpg");
            //
            // FileOutputStream fos = new FileOutputStream(f);
            // bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            // JSONObject jsonRes = new JSONObject();
            // jsonRes.put("filePath", f.getAbsolutePath());
            // PluginResult result = new PluginResult(PluginResult.Status.OK, jsonRes);
            // pluginContext.sendPluginResult(result);
            // fos.close();
            // Log.d(TAG, "Screenshot saved as: " + f.getAbsolutePath());
            //
            // }
        } catch (Exception e) {
            pluginContext.error(e.getMessage());

        }
    }

    @TargetApi(26)
    public void takeScreenshot() {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        int[] location = new int[2];

        Rect rect = new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());

        PixelCopy.OnPixelCopyFinishedListener listener = new PixelCopy.OnPixelCopyFinishedListener() {

            @Override
            public void onPixelCopyFinished(int copyResult) {

                if (copyResult == PixelCopy.SUCCESS) {
                    saveScreenshot(bitmap);
                } else {
                    pluginContext.error("PixelCopy resulted with error: " + copyResult);
                }
            }
        };

        try {
            PixelCopy.request(cordova.getActivity().getWindow(), rect, bitmap, listener, new Handler());
        } catch (IllegalArgumentException e) {

            e.printStackTrace();
        }
    }

}
