package com.sarriaroman.PhotoViewer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoActivity extends Activity {
    private PhotoViewAttacher mAttacher;

    private ImageView photo;

    private ImageButton closeBtn;
    private ProgressBar loadingBar;

    private TextView titleTxt;

    private String mImage;
    private String mTitle;
    private JSONObject mHeaders;
    private JSONObject pOptions;
    private File mTempImage;

    public static JSONArray mArgs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getApplication().getResources().getIdentifier("activity_photo", "layout", getApplication().getPackageName()));

        // Load the Views
        findViews();

        try {
            this.mImage = mArgs.getString(0);
            this.mTitle = mArgs.getString(1);
            this.mHeaders = parseHeaders(mArgs.optString(5));
            this.pOptions = mArgs.optJSONObject(6);

            if( pOptions == null ) {
                pOptions = new JSONObject();
                pOptions.put("fit", true);
                pOptions.put("centerInside", true);
                pOptions.put("centerCrop", false);
            }


        } catch (JSONException exception) {
            
        }
        //Change the activity title
        if (!mTitle.equals("")) {
            titleTxt.setText(mTitle);
        }

        try {
            loadImage();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Set Button Listeners
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /**
     * Find and Connect Views
     */
    private void findViews() {
        // Buttons first
        closeBtn = (ImageButton) findViewById(getApplication().getResources().getIdentifier("closeBtn", "id", getApplication().getPackageName()));

        //ProgressBar
        loadingBar = (ProgressBar) findViewById(getApplication().getResources().getIdentifier("loadingBar", "id", getApplication().getPackageName()));
        // Photo Container
        photo = (ImageView) findViewById(getApplication().getResources().getIdentifier("photoView", "id", getApplication().getPackageName()));
        mAttacher = new PhotoViewAttacher(photo);

        // Title TextView
        titleTxt = (TextView) findViewById(getApplication().getResources().getIdentifier("titleTxt", "id", getApplication().getPackageName()));
    }

    /**
     * Get the current Activity
     *
     * @return
     */
    private Activity getActivity() {
        return this;
    }

    /**
     * Hide Loading when showing the photo. Update the PhotoView Attacher
     */
    private void hideLoadingAndUpdate() {
        photo.setVisibility(View.VISIBLE);
        loadingBar.setVisibility(View.INVISIBLE);

        mAttacher.update();
    }

    private RequestCreator setOptions(RequestCreator picasso) throws JSONException {
        if(this.pOptions.has("fit") && this.pOptions.optBoolean("fit")) {
            picasso.fit();
        }

        if(this.pOptions.has("centerInside") && this.pOptions.optBoolean("centerInside")) {
            picasso.centerInside();
        }

        if(this.pOptions.has("centerCrop") && this.pOptions.optBoolean("centerCrop")) {
            picasso.centerCrop();
        }

        return picasso;
    }

    /**
     * Load the image using Picasso
     */
    private void loadImage() throws JSONException {
        if (mImage.startsWith("http") || mImage.startsWith("file")) {
            this.setOptions(Picasso.get().load(mImage)).into(photo, new Callback() {
                @Override
                public void onSuccess() {
                    hideLoadingAndUpdate();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getActivity(), "Error loading image.", Toast.LENGTH_LONG).show();

                    finish();
                }
            });
        } else if (mImage.startsWith("data:image")) {

            new AsyncTask<Void, Void, File>() {

                protected File doInBackground(Void... params) {
                    String base64Image = mImage.substring(mImage.indexOf(",") + 1);
                    return getLocalBitmapFileFromString(base64Image);
                }

                protected void onPostExecute(File file) {
                    mTempImage = file;

                    try {
                        setOptions(Picasso.get().load(mTempImage))
                                .into(photo, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        hideLoadingAndUpdate();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(getActivity(), "Error loading image.", Toast.LENGTH_LONG).show();

                                        finish();
                                    }
                                });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.execute();

        } else {
            photo.setImageURI(Uri.parse(mImage));

            hideLoadingAndUpdate();
        }
    }

    public void onDestroy() {
        if (mTempImage != null) {
            mTempImage.delete();
        }
        super.onDestroy();
    }


    public File getLocalBitmapFileFromString(String base64) {
        File file;
        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream output = new FileOutputStream(file);
            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
            output.write(decoded);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            file = null;
        }
        return file;
    }

    private JSONObject parseHeaders(String headerString) {
        JSONObject headers = null;

        // Short circuit if headers is empty
        if (headerString == null || headerString.length() == 0) {
            return headers;
        }

        // headers should never be a JSON array, only a JSON object
        try {
            headers = new JSONObject(headerString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return headers;
    }
}