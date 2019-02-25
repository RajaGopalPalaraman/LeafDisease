package com.edot.imageprocessor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    private Context context;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.context = context;
        this.camera = camera;
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("Leaf","At SurfaceCreated");
        try {
            camera.setPreviewDisplay(holder);
            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPictureSizes();
            Camera.Size size = sizes.get(0);
            for (int i = 0; i < sizes.size(); i++) {
                if (sizes.get(i).width > size.width)
                    size = sizes.get(i);
            }
            params.setPictureSize(size.width, size.height);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            params.set("orientation", "portrait");
            params.set("rotation",90);
            camera.setParameters(params);
            camera.enableShutterSound(true);
            camera.setDisplayOrientation(90);
            camera.startPreview();
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            Log.d("Leaf","Image Captured");
                            captureImage(data);
                            Intent intent = new Intent(context,ImageViewerActivity.class);
                            context.startActivity(intent);
                        }
                    });
                }
            });
        } catch (IOException e) {
            Toast.makeText(context,R.string.PREVIEW_ERROR,Toast.LENGTH_SHORT).show();
            ((Activity) context).finish();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("Leaf","At SurfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("Leaf","At SurfaceDestroyed");
    }

    public void captureImage(byte[] data) {
        try {
            File file = new File(context.getFilesDir(), "photo.jpg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data);
            fileOutputStream.close();
        } catch (Exception e) {
            Log.e("Leaf",e.getLocalizedMessage());
        }
    }

}
