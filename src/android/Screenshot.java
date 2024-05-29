/**
 * Copyright (C) 2012 30ideas (http://30ide.as)
 * MIT licensed
 *
 * @author Josemando Sobral
 * @created Jul 2nd, 2012.
 * improved by Hongbo LU
 */
package com.darktalker.cordova.screenshot;

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

    public void getScreenshotAsURI() {

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
            saveScreenshot();
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
