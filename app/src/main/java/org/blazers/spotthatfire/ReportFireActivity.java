package org.blazers.spotthatfire;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;


public class ReportFireActivity extends AppCompatActivity {
    FirebaseVisionLabelDetectorOptions options;

    //Image and GPS capture code open....................................................................

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    Context context = null;
    String device_id;

    // directory name to store captured images
    private static final String IMAGE_DIRECTORY_NAME = "Asisoft";

    private Uri fileUri; // file url to store image

    private ImageView imgPreview;
    // private Button btnCapturePicture;

    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

    TextView TxtLatlong, tvLabels;
    Button btReport;
    ProgressBar progressBar;

    GPSTracker gps, gpsFpi;


    //............submit picture to database.................
    byte[] byteArray;
    String encodedImage;
    private List<FirebaseVisionLabel> allLabels;
    private double longitude;
    private double latitude;
    private String LOG_TAG = "log_tag";
    private String REQUEST_TAG = "fire_request";
    //............submit picture to database.................


    //Image and GPS capture code close....................................................................

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_fire);

        options = new FirebaseVisionLabelDetectorOptions.Builder()
                .setConfidenceThreshold(0.8f)
                .build();

        TxtLatlong = (TextView) findViewById(R.id.lat_lon_container);
        imgPreview = (ImageView) findViewById(R.id.image_video_container);
        tvLabels = (TextView) findViewById(R.id.tvLabels);
        btReport = (Button) findViewById(R.id.report_fire_button);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Image and GPS capture code open....................................................................
        context = getApplicationContext();


        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            device_id = tm.getDeviceId();
        } catch (SecurityException ex) {

        }

        //make API call
        btReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tvLabels.getText().toString().contains("Fire")) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.bringToFront();
                    makeCall(latitude, longitude);
                    progressBar.setVisibility(View.INVISIBLE);
                }
                else
                    Toast.makeText(context, "Sorry, this does not look like a fire.", Toast.LENGTH_SHORT).show();
            }
        });


        /**
         * Capture image button click event
         * */
        imgPreview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                captureImage();

                gps = new GPSTracker(ReportFireActivity.this);
                // check if GPS enabled
                if (gps.canGetLocation()) {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    // \n is for new line
                    // //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, //Toast.LENGTH_LONG).show();
                    TxtLatlong.setText("" + latitude + " / " + "" + longitude);
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
            }


        });


        //Image and GPS capture code close....................................................................

    }

    private void makeCall(double latitude, double longitude) {
        String url = "http://wildfire-api-app.herokuapp.com/cordinates/" + latitude + "/" + longitude;
        final String[] result = {""};

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(LOG_TAG, response.toString());

                        if (response != null) {
                            try {
                                if(response.has("message") && response.getString("message").equals("success")){
                                    result[0] = "Thanks for your request!\nAppropriate authorities have been notified.\nPlease get to safety!";
                                }else{
                                    result[0] = "Sorry, there was an error";
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d(LOG_TAG, "Exception encountered: " + e.toString());
                            } finally {

                                Toast.makeText(context, result[0], Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {

                            Toast.makeText(context, "Sorry, there is a problem with your network.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "onErrorResponse: Error listener fired: " + error.getMessage());
                if (error.toString().contains("NoConnectionError")) {
                    Toast.makeText(ReportFireActivity.this, "Your internet connection might be down", Toast.LENGTH_LONG).show();

                }
                VolleyLog.d(LOG_TAG, "Error: " + error.getMessage());

            }
        });
        // Adding JsonObject request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest, REQUEST_TAG);
    }

    //Image and GPS capture code open....................................................................
    //Receiving activity result method will be called after closing the camera

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {


                //Toast.makeText(context, "Result OK", //Toast.LENGTH_SHORT).show();
                // successfully captured the image
                // display it in image view
                previewCapturedImage();

                //............submit picture to database.................

                Bitmap originBitmap = null;


                if (originBitmap != null) {

                    //this.imgPreview.setImageBitmap(originBitmap);

                    //picasso
                    Picasso.with(ReportFireActivity.this).load(fileUri).fit().centerCrop().into(imgPreview);


                    Log.w("Image Setted in", "Done Loading Image");
                    try {
                        Bitmap image = ((BitmapDrawable) imgPreview.getDrawable()).getBitmap();

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byteArray = byteArrayOutputStream.toByteArray();
                        encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                        // Calling the background process so that application wont slow down
                        //UploadImage uploadImage = new UploadImage();
                        //uploadImage.execute("");
                        //End Calling the background process so that application wont slow down
                    } catch (Exception e) {
                        Log.w("OOooooooooo", "exception");
                    }
                    //Toast.makeText(ReportFireActivity.this, "Conversion Done", //Toast.LENGTH_SHORT).show();
                }
                // End getting the selected image, setting in imageview and converting it to byte and base 64

                //............submit picture to database.................


            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(), "Cancelled", LENGTH_SHORT).show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(), "Error!", LENGTH_SHORT).show();
            }
        }

    }

    private void labelImage(Bitmap bitmap) {
        //Toast.makeText(context, "labelling started", //Toast.LENGTH_SHORT).show();
        tvLabels.setText("");
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance()
                .getVisionLabelDetector();
// Or, to set the minimum confidence required:
//        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance()
//                .getVisionLabelDetector(options);
        Task<List<FirebaseVisionLabel>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionLabel>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionLabel> labels) {
                                        // Task completed successfully
                                        // ...
                                        //Toast.makeText(context, "labeled successfully", //Toast.LENGTH_SHORT).show();
                                        allLabels = labels;
                                        for (FirebaseVisionLabel label : labels) {
                                            String text = label.getLabel();
                                            String entityId = label.getEntityId();
                                            float confidence = label.getConfidence();

                                            tvLabels.setText(tvLabels.getText().toString() + "\n" + text + " : " + entityId + " : " + confidence);
                                            //Toast.makeText(context, "Labelling successful", //Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Toast.makeText(context, "Lablling failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
    }


    //Here we store the file url as it will be null after returning from camera app

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    //Here I restore the fileUri again

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    //Method to fire up camera

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);

    }

    //Creating file uri to store image/video

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /*
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Fisierul "
                        + IMAGE_DIRECTORY_NAME + " nu a fost creat");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFileName;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFileName = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFileName;
    }

    //Display image from a path to ImageView

    private void previewCapturedImage() {
        try {
            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            labelImage(bitmap);

            imgPreview.setImageBitmap(bitmap);


        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    //Image and GPS capture code close....................................................................

}
