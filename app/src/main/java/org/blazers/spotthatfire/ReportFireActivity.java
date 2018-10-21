package org.blazers.spotthatfire;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ReportFireActivity extends AppCompatActivity {


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

    TextView TxtLatlong;

    GPSTracker gps, gpsFpi;


    //............submit picture to database.................
    byte[] byteArray;
    String encodedImage;
    //............submit picture to database.................


    //Image and GPS capture code close....................................................................

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_fire);

        TxtLatlong = findViewById(R.id.lat_lon_container);
        imgPreview = findViewById(R.id.image_video_container);

        //Image and GPS capture code open....................................................................
        context = getApplicationContext();


        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            device_id = tm.getDeviceId();
        } catch (SecurityException ex) {

        }


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
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    // \n is for new line
                    // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
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

    //Image and GPS capture code open....................................................................
    //Receiving activity result method will be called after closing the camera

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();

                //............submit picture to database.................

                Bitmap originBitmap = null;


                if (originBitmap != null)
                {
                    this.imgPreview.setImageBitmap(originBitmap);
                    Log.w("Image Setted in", "Done Loading Image");
                    try
                    {
                        Bitmap image = ((BitmapDrawable) imgPreview.getDrawable()).getBitmap();
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byteArray = byteArrayOutputStream.toByteArray();
                        encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        // Calling the background process so that application wont slow down
                        //UploadImage uploadImage = new UploadImage();
                        //uploadImage.execute("");
                        //End Calling the background process so that application wont slow down
                    }
                    catch (Exception e)
                    {
                        Log.w("OOooooooooo","exception");
                    }
                    Toast.makeText(ReportFireActivity.this, "Conversion Done",Toast.LENGTH_SHORT).show();
                }
                // End getting the selected image, setting in imageview and converting it to byte and base 64

                //............submit picture to database.................




            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "Cancelled", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Error!", Toast.LENGTH_SHORT)
                        .show();
            }
        }

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

            imgPreview.setImageBitmap(bitmap);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }








    //Image and GPS capture code close....................................................................

}
