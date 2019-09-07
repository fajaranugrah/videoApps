package com.example.fajarramadhan.videoapps;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RecoverySystem;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatForSendImageOrVideo extends AppCompatActivity {
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar progressBar, progressBarCircle, progressBarVid, progressBarVid2;
    String filePath;
    boolean isImage;
    private TextView txtPercentage;
    private ImageView imgPreview;
    private VideoView vidPreview;
    private Button btnUpload;
    long totalSize = 0;
    int progress = 10;
    DownloadFilesTask downloadFilesTask;
    Button imageButton, retry;
    Handler handler = new Handler();
    Runnable mRunnableLoadDelay = new Runnable() {
        @Override
        public void run() {
            //int pro = progress + 10;
            progressBarVid2.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        vidPreview = (VideoView) findViewById(R.id.videoPreview);
        progressBarCircle = (ProgressBar) findViewById(R.id.progressbar_circle);
        progressBarVid = (ProgressBar) findViewById(R.id.progressbar_circle_video);
        progressBarVid2 = (ProgressBar) findViewById(R.id.progressbar_circle_video_2);

        if (getIntent().hasExtra("filePath")){
            filePath = getIntent().getStringExtra("filePath");
            Log.e("checkFilePath", filePath);
        }

        // boolean flag to identify the media type, image or video
        if (getIntent().hasExtra("isImage")){
            isImage = getIntent().getBooleanExtra("isImage", false);
        }

        if (filePath != null) {
            // Displaying the image or video on the screen
            previewMedia(isImage);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }

        imageButton = (Button) findViewById(R.id.cancel);

        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // uploading the file to server

                /*progressBarVid.setVisibility(View.VISIBLE);
                int count = 10;
                for (int i = 0; i<= count; i++){
                    handler.postDelayed(mRunnableLoadDelay,1000);
                }*/
                downloadFilesTask = new DownloadFilesTask();
                downloadFilesTask.execute();
            }
        });

        retry = (Button) findViewById(R.id.retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFilesTask = new DownloadFilesTask();
                downloadFilesTask.execute();
            }
        });

    }

    /**
     * Displaying captured image/video on the screen
     * */
    private void previewMedia(boolean isImage) {
        // Checking whether captured media is image or video
        if (isImage) {
            imgPreview.setVisibility(View.VISIBLE);
            vidPreview.setVisibility(View.GONE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

            //imgPreview.setImageBitmap(bitmap);
            progressBarCircle.setIndeterminate(false);
            progressBarCircle.setVisibility(View.VISIBLE);
            Picasso.with(ChatForSendImageOrVideo.this)
                    .load(filePath)
                    .placeholder(R.drawable.progress_animated)
                    .into(imgPreview);
        } else {
            imgPreview.setVisibility(View.GONE);
            vidPreview.setVisibility(View.VISIBLE);
            Uri fileUri = Uri.parse(filePath);
            vidPreview.setVideoURI(fileUri);
            // start playing
            vidPreview.start();
        }
    }

    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBarVid.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBarVid.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBarVid.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return null;
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = "1";



            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);

            // showing the server response in an alert dialog
            showAlert(result);

            super.onPostExecute(result);
        }

    }

    boolean stop = false;
    public class DownloadFilesTask extends AsyncTask<URL, Integer, Long> {
        protected Long doInBackground(URL... urls) {
            int count = 10000;
            long totalSize = 0;

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isCancelled();
                    stop = true;
                    retry.setVisibility(View.VISIBLE);
                }
            });

            for (int i = 0; i < count; i++) {
                totalSize += 1;
                if (!stop){
                    publishProgress((int) ((i / (float) count) * 100));
                } else {
                    isCancelled();
                    break;
                }
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBarVid.setVisibility(View.VISIBLE);
            imageButton.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBarVid.setProgress(progress[0]);
        }

        protected void onPostExecute(Long result) {
            //showAlert(result.toString());
            imageButton.setVisibility(View.GONE);
            progressBarVid.setVisibility(View.GONE);
            progressBarVid2.setVisibility(View.VISIBLE);
            handler.postDelayed(mRunnableLoadDelay,8000);
        }
    }

    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
