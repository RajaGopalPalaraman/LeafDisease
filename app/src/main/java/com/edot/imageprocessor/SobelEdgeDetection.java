package com.edot.imageprocessor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class SobelEdgeDetection {

    public static void detectEdge(Bitmap image)
    {
        doFilter(image);
    }

    private static int  getGrayScale(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;

        //from https://en.wikipedia.org/wiki/Grayscale, calculating luminance
        int gray = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
        //int gray = (r + g + b) / 3;

        return gray;
    }

    private static void doFilter(Bitmap image)
    {
        int x = image.getWidth();
        int y = image.getHeight();
        int[][] edgeColors = new int[x][y];
        int maxGradient = -1;
        int minGradient = Integer.MAX_VALUE;

        for (int i = 1; i < x - 1; i++) {
            for (int j = 1; j < y - 1; j++) {
                int val00 = getGrayScale(image.getPixel(i - 1, j - 1));
                int val01 = getGrayScale(image.getPixel(i - 1, j));
                int val02 = getGrayScale(image.getPixel(i - 1, j + 1));

                int val10 = getGrayScale(image.getPixel(i, j - 1));
                int val11 = getGrayScale(image.getPixel(i, j));
                int val12 = getGrayScale(image.getPixel(i, j + 1));

                int val20 = getGrayScale(image.getPixel(i + 1, j - 1));
                int val21 = getGrayScale(image.getPixel(i + 1, j));
                int val22 = getGrayScale(image.getPixel(i + 1, j + 1));

                int gx =  ((-1 * val00) + (0 * val01) + (1 * val02))
                        + ((-2 * val10) + (0 * val11) + (2 * val12))
                        + ((-1 * val20) + (0 * val21) + (1 * val22));

                int gy =  ((-1 * val00) + (-2 * val01) + (-1 * val02))
                        + ((0 * val10) + (0 * val11) + (0 * val12))
                        + ((1 * val20) + (2 * val21) + (1 * val22));

                double gval = Math.sqrt((gx * gx) + (gy * gy));
                int g = (int) gval;

                if(maxGradient < g) {
                    maxGradient = g;
                }
                if(g < minGradient)
                {
                    minGradient = g;
                }
                edgeColors[i][j] = g;
            }
        }
        double scale = 255.0 / maxGradient;
        Log.d("edge","max:"+maxGradient);
        Log.d("edge","min:"+minGradient);
        Log.d("edge","min-gray:"+minGradient*scale);

        for (int i = 1; i < x - 1; i++) {
            for (int j = 1; j < y - 1; j++) {
                int edgeColor = edgeColors[i][j];
                edgeColor = (int)(edgeColor * scale);
                edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;
                image.setPixel(i, j, edgeColor);
            }
        }
    }

}
