package com.darktalker.cordova.screenshot;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.view.PixelCopy;
import android.view.View;
import android.util.Base64;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

import androidx.core.content.ContextCompat;

import android.annotation.TargetApi;

@TargetApi(24)
public class ScreenshotSaver {
    CordovaInterface cordova;
    View view;
    String fileName;
    CallbackContext pluginContext;

    public ScreenshotSaver(CordovaInterface cordova, View view, String fileName, CallbackContext pluginContext) {
        this.cordova = cordova;
        this.view = view;
        this.fileName = fileName;
        this.pluginContext = pluginContext;
    }

    public void saveScreenshot(Bitmap bitmap) {
        try {

            File storageDir = new File(
                    ContextCompat.getExternalFilesDirs(cordova.getActivity().getApplicationContext(), null)[0]
                            + File.separator + "screenshots",
                    fileName + ".jpg");

            if (!Objects.requireNonNull(storageDir.getParentFile()).exists()) {
                storageDir.getParentFile().mkdirs();
            }

            try {
                FileOutputStream fos = new FileOutputStream(storageDir);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                JSONObject jsonRes = new JSONObject();
                jsonRes.put("filePath", storageDir.getAbsolutePath());
                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonRes);
                pluginContext.sendPluginResult(result);
                fos.close();
            } catch (Exception e) {
                pluginContext.error(e.getMessage());
            }

        } catch (Exception e) {
            pluginContext.error(e.getMessage());

        }
    }

    private void getScreenshotAsURI(Bitmap bitmap) {
        try {
            ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();

            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, jpeg_data)) {
                byte[] code = jpeg_data.toByteArray();
                byte[] output = Base64.encode(code, Base64.NO_WRAP);
                String js_out = new String(output);
                js_out = "data:image/jpeg;base64," + js_out;
                JSONObject jsonRes = new JSONObject();
                jsonRes.put("URI", js_out);
                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonRes);
                pluginContext.sendPluginResult(result);
            }

        } catch (Exception e) {
            pluginContext.error(e.getMessage());

        }
    }

    @TargetApi(26)
    public void takeScreenshot(Boolean shouldReturnBase64Uri) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        int[] location = new int[2];

        Rect rect = new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());

        PixelCopy.OnPixelCopyFinishedListener listener = new PixelCopy.OnPixelCopyFinishedListener() {

            @Override
            public void onPixelCopyFinished(int copyResult) {

                if (copyResult == PixelCopy.SUCCESS) {
                    if (shouldReturnBase64Uri) {
                        getScreenshotAsURI(bitmap);
                    } else {
                        saveScreenshot(bitmap);
                    }

                } else {
                    pluginContext.error("PixelCopy resulted with error: " + copyResult);
                }
            }
        };

        try {
            PixelCopy.request(cordova.getActivity().getWindow(), rect, bitmap, listener, new Handler());
        } catch (IllegalArgumentException e) {

            pluginContext.error("PixelCopy resulted with error: " + e.getMessage());
        }
    }

}
