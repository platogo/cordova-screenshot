/**
 * Copyright (C) 2012 30ideas (http://30ide.as)
 * MIT licensed
 *
 * @author Josemando Sobral
 * @created Jul 2nd, 2012.
 * improved by Hongbo LU
 */
package com.darktalker.cordova.screenshot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Screenshot extends CordovaPlugin {
    private CallbackContext mCallbackContext;
    private JSONArray mArgs;
    private String mFileName;

    protected final static String[] PERMISSIONS = { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    public static final int PERMISSION_DENIED_ERROR = 20;
    public static final int SAVE_SCREENSHOT_SEC = 0;

    private void takeAndSaveScreenshot(String fileName, Boolean shouldReturnBase64Uri) {
        ScreenshotSaver saver = new ScreenshotSaver(cordova, webView.getView(), fileName, mCallbackContext);
        saver.takeScreenshot(shouldReturnBase64Uri);
    }

    public void saveScreenshot() throws JSONException {
        mFileName = (String) mArgs.get(0);

        super.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                takeAndSaveScreenshot(mFileName, false);
            }
        });
    }

    public void getScreenshotAsURI()  {

        super.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                takeAndSaveScreenshot(mFileName, true);
            }
        });
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        // starting on ICS, some WebView methods
        // can only be called on UI threads
        mCallbackContext = callbackContext;
        mArgs = args;

        if (action.equals("saveScreenshot")) {
            // Check if we are on Android 11 (Android R, sdk 29) or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                saveScreenshot();
            } else {
                if (PermissionHelper.hasPermission(this, PERMISSIONS[0])) {
                    saveScreenshot();
                } else {
                    PermissionHelper.requestPermissions(this, SAVE_SCREENSHOT_SEC, PERMISSIONS);
                }
            }
            return true;
        } else if (action.equals("getScreenshotAsURI")) {
            getScreenshotAsURI();
            return true;
        } else if (action.equals("getAvailableInternalMemorySize")) {
            getAvailableInternalMemorySize();
            return true;
        }
        callbackContext.error("action not found");
        return false;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                mCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        if (requestCode == SAVE_SCREENSHOT_SEC) {
            saveScreenshot();
        }
    }

    // Returns free space in bytes.
    public void getAvailableInternalMemorySize() throws JSONException {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize, availableBlocks;
        blockSize = stat.getBlockSizeLong();
        availableBlocks = stat.getAvailableBlocksLong();

        JSONObject jsonRes = new JSONObject();
        jsonRes.put("freeSpace", availableBlocks * blockSize);
        PluginResult result = new PluginResult(PluginResult.Status.OK, jsonRes);
        mCallbackContext.sendPluginResult(result);
    }


}
