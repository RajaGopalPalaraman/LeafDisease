package com.edot.imageprocessor;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import java.util.HashMap;
import java.util.logging.Level;

public class ImageProcessorThread extends Thread {

    private final Handler handler;
    private final Bitmap bitmap;

    public ImageProcessorThread(Handler handler, Bitmap bitmap)
    {
        this.handler = handler;
        this.bitmap = bitmap.copy(bitmap.getConfig(),true);
    }

    @Override
    public void run() {
            super.run();
            Bitmap originalBitmap = bitmap.copy(bitmap.getConfig(), true);
            Bitmap copyToUi = null;
            handler.obtainMessage(1, "Detecting Edges...").sendToTarget();
            SobelEdgeDetection.detectEdge(bitmap);
            ImageProcessorUtil.smoothImage(bitmap);
            copyToUi = bitmap.copy(bitmap.getConfig(), true);
            handler.obtainMessage(0, copyToUi).sendToTarget();
            handler.obtainMessage(1, "Enhancing....").sendToTarget();
            ImageProcessorUtil.invert(bitmap);
            copyToUi = bitmap.copy(bitmap.getConfig(), true);
            handler.obtainMessage(0, copyToUi).sendToTarget();
            handler.obtainMessage(1, "Masking....").sendToTarget();
            ImageProcessorUtil.applyWhiteMask(bitmap);
            copyToUi = bitmap.copy(bitmap.getConfig(), true);
            handler.obtainMessage(0, copyToUi).sendToTarget();
            handler.obtainMessage(1, "Applying Colours.....").sendToTarget();
            ImageProcessorUtil.applyColor(bitmap, originalBitmap);
            copyToUi = bitmap.copy(bitmap.getConfig(), true);
            handler.obtainMessage(0, copyToUi).sendToTarget();
            handler.obtainMessage(1, "Analyzing.....").sendToTarget();
            HashMap<String, Integer> data = ImageProcessorUtil.analyze(bitmap);

            int brownCount = data.get(ImageProcessorUtil.BROWN_COUNT);
            int yellowCount = data.get(ImageProcessorUtil.YELLOW_COUNT);
            int greenCount = data.get(ImageProcessorUtil.GREEN_COUNT);

            int total = brownCount + yellowCount + greenCount;
            float damaged = brownCount + yellowCount;

            float damagePercentage = (damaged / total) * 100;
            int roundedPercentage = (int) damagePercentage;

            Log.d("Leaf", "" + roundedPercentage);
            //showDamagePercentage(roundedPercentage);
            diseaseName(brownCount,yellowCount,greenCount);

            handler.sendEmptyMessage(2);

    }

    private void diseaseName(float brown, float yellow, float green)
    {
        float total = brown + yellow + green;
        float brownPercentage = (brown / total) * 100;
        float yellowPercentage = (yellow / total) * 100;

        Log.d("Brown","Brown Percentage: "+brownPercentage);
        Log.d("Brown","Yellow Percentage: "+yellowPercentage);

        if (yellow > brown)
        {
            handler.obtainMessage(1, "Result: Aster Yellow").sendToTarget();
        }
        else if (brownPercentage <= 10)
        {
            handler.obtainMessage(1, "Result: Crown gall").sendToTarget();
        }
        else if (brownPercentage <= 30)
        {
            handler.obtainMessage(1, "Result: Fire Blight").sendToTarget();
        }
        else if (brownPercentage <= 50)
        {
            handler.obtainMessage(1, "Result: Verticillium Wilt").sendToTarget();
        }
        else
        {
            handler.obtainMessage(1, "Result: Blister Rust").sendToTarget();
        }
    }

    private void showDamagePercentage(int roundedPercentage)
    {
        if (roundedPercentage > 0) {
            handler.obtainMessage(1, "Result: Leaf damaged by " + roundedPercentage + "%").sendToTarget();
        } else {
            handler.obtainMessage(1, "Result: Healthy Leaf").sendToTarget();
        }
    }

}
