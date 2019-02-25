package com.edot.imageprocessor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;


public class ImageViewerActivity extends AppCompatActivity {

    private static final int REQ_WIDTH = 400;
    private static final int REQ_HEIGHT = 400;

    private FileReader fileReaderAsyncTask = null;

    private ImageView imageView;
    private Handler handler;
    private Bitmap bitmap;

    private Button reTakeButton;
    private Button analyzeButton;
    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_image_viewer);
            imageView = findViewById(R.id.imageView);
            analyzeButton = findViewById(R.id.analyze);
            reTakeButton = findViewById(R.id.retake);
            statusView = findViewById(R.id.statusView);
            handler = new Handler(getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (0 == msg.what) {
                        Bitmap bitmap = (Bitmap) msg.obj;
                        //bitmap = bitmap.copy(bitmap.getConfig(),true);
                        imageView.setImageBitmap(bitmap);
                    }
                    else if(1 == msg.what)
                    {
                        statusView.setText((String) msg.obj);
                    }
                    else {
                        Log.d("Leaf","Button Enabled");
                        analyzeButton.setClickable(true);
                        reTakeButton.setClickable(true);
                    }
                }
            };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(bitmap == null)
        {
            final Intent intent = getIntent();
            fileReaderAsyncTask = new FileReader(intent);
            fileReaderAsyncTask.read();
        }
    }

    public void analyze(View view) {
        analyzeButton.setClickable(false);
        reTakeButton.setClickable(false);
        statusView.setText(getString(R.string.processing));
        //Bitmap bmp2 = bitmap.copy(bitmap.getConfig(), true);
        new ImageProcessorThread(handler, bitmap).start();
    }

    public void callFinish(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fileReaderAsyncTask.cancel(true);
    }

    private class FileReader extends AsyncTask<Void,Void,Boolean>
    {
        Intent intent;

        private FileReader(Intent intent) {
            this.intent = intent;
        }

        public void read()
        {
            execute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                if (intent != null) {
                    String uri = intent.getStringExtra("fileName");
                    if (uri != null) {
                        ParcelFileDescriptor parcelFileDescriptor =
                                getContentResolver().openFileDescriptor(Uri.parse(uri), "r");
                        if (parcelFileDescriptor != null) {
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                            BitmapFactory.decodeFileDescriptor(fileDescriptor,null,options);
                            Log.d("Leaf","Height:"+options.outHeight);
                            Log.d("Leaf","Width:"+options.outWidth);
                            options.inSampleSize = 1;
                            if(options.outWidth > REQ_WIDTH && options.outHeight > REQ_HEIGHT)
                            {
                                options.inSampleSize = calculateInSampleSize(options, REQ_WIDTH, REQ_HEIGHT);
                            }
                            Log.d("Leaf","SampleSize:"+options.inSampleSize);
                            options.inJustDecodeBounds = false;
                            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor,null,options);
                            parcelFileDescriptor.close();
                            return true;
                        }
                    }
                }
                File file = new File(getFilesDir(), "photo.jpg");
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);
                Log.d("Leaf","Height:"+options.outHeight);
                Log.d("Leaf","Width:"+options.outWidth);
                options.inSampleSize = 1;
                if(options.outWidth > REQ_WIDTH && options.outHeight > REQ_HEIGHT)
                {
                    options.inSampleSize = calculateInSampleSize(options, REQ_WIDTH, REQ_HEIGHT);
                }
                Log.d("Leaf","SampleSize:"+options.inSampleSize);
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),options);

                return true;
            } catch (Exception e) {
                Log.e("Leaf", e.getLocalizedMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean) {
                Log.d("Leaf","BitmapHeight:"+bitmap.getHeight());
                Log.d("Leaf","BitmapWidth:"+bitmap.getWidth());
                imageView.setImageBitmap(bitmap);
                statusView.setText("");
            }
            else
            {
                Toast.makeText(ImageViewerActivity.this,R.string.image_loading_failed,Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
