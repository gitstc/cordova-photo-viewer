package com.sarriaroman.PhotoViewer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Class to Open PhotoViewer with the Required Parameters from Cordova
 * <p>
 * - URL
 * - Title
 */
public class PhotoViewer extends CordovaPlugin {
    protected JSONArray args;
    protected CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("show")) {
            this.args = args;
            this.callbackContext = callbackContext;

            this.launchActivity();
            return true;
        }
        return false;
    }

    //
    protected void launchActivity() throws JSONException {
        Intent i = new Intent(this.cordova.getActivity(), com.sarriaroman.PhotoViewer.PhotoActivity.class);
        PhotoActivity.mArgs = this.args;

        this.cordova.getActivity().startActivity(i);
        this.callbackContext.success("");
    }


}