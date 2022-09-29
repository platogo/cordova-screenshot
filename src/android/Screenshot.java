/**
 * Copyright (C) 2012 30ideas (http://30ide.as)
 * MIT licensed
 *
 * @author Josemando Sobral
 * @created Jul 2nd, 2012.
 * improved by Hongbo LU
 */
package com.darktalker.cordova.screenshot;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class Screenshot extends CordovaPlugin {
    private CallbackContext mCallbackContext;
    private JSONArray mArgs;
    private String mFileName;

    private void takeAndSaveScreenshot(String fileName) {
        ScreenshotSaver saver = new ScreenshotSaver(cordova, webView.getView(), fileName, mCallbackContext);
        saver.takeScreenshot();
    }

    public void saveScreenshot() throws JSONException {
        mFileName = (String) mArgs.get(0);

        super.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                takeAndSaveScreenshot(mFileName);
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
        }
        callbackContext.error("Android supports saveScreenshot only");
        return false;
    }

}
