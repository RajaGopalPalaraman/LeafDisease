package com.edot.imageprocessor;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Properties;

public class ImageProcessorUtil {

    public static final String BROWN_COUNT = "brown-pixel-count";
    public static final String YELLOW_COUNT = "yellow-pixel-count";
    public static final String GREEN_COUNT = "green-pixel-count";

    public static boolean isRedPixel(int pixel)
    {
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = (pixel) & 0xff;
        return r>g && r>b;
    }

    public static boolean isGreenPixel(int pixel)
    {
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = (pixel) & 0xff;
        return g>r && g>b;
    }

    public static boolean isBluePixel(int pixel)
    {
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = (pixel) & 0xff;
        return b>r && b>g;
    }

    public static int getGrayScale(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;

        return (r + g + b) / 3;
    }

    public static void applyColor(Bitmap source, Bitmap target)
    {
        int x = source.getWidth();
        int y = source.getHeight();

        for (int i=0;i<x;i++)
        {
            for (int j=0;j<y;j++)
            {
                int pixel = source.getPixel(i,j)&target.getPixel(i,j);
                source.setPixel(i,j,pixel);
            }
        }
    }

    public static void invert(Bitmap bitmap)
    {
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();

        for (int i=0;i<x;i++) {
            for (int j = 0; j < y; j++) {
                int pixel = bitmap.getPixel(i,j);
                pixel = getGrayScale(pixel);
                if(pixel > 40)
                {
                    pixel = 255;
                }
                else
                {
                    pixel = 0;
                }
                pixel = 0xff000000 | (pixel << 16) | (pixel << 8) | pixel;
                bitmap.setPixel(i, j, pixel);
            }
        }
    }

    public static void smoothImage(Bitmap bitmap)
    {
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();
        Bitmap newBitmap = bitmap.copy(bitmap.getConfig(),true);
        for (int i=1;i<x-1;i++) {
            for (int j=1; j < y-1; j++) {
                averageFilter(bitmap,newBitmap,i,j);
            }
        }
    }

    private static void averageFilter(Bitmap source,Bitmap reference,int x,int y)
    {
        int pixel = 0;

        for (int i=-1;i<2;i++)
        {
            for (int j=-1;j<2;j++)
            {
                pixel = pixel + getGrayScale(reference.getPixel(x+i,y+j));
            }
        }

        pixel = pixel/9;
        pixel = 0xff000000 | (pixel << 16) | (pixel << 8) | pixel;
        source.setPixel(x,y,pixel);
    }

    public static void applyWhiteMask(Bitmap bitmap)
    {
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();
        for (int i=0;i<y;i++) {
            int j;
            int k;
            for (j=0;j<x;j++) {
                if(getGrayScale(bitmap.getPixel(j,i))==255)
                {
                    break;
                }
            }
            for (k=x-1;k>j;k--)
            {
                if(getGrayScale(bitmap.getPixel(k,i))==255)
                {
                    break;
                }
            }
            fillWithWhitePixel(bitmap,j,k,i);
        }
    }

    private static void fillWithWhitePixel(Bitmap bitmap,int x1,int x2,int y)
    {
        for (int i=x1;i<=x2;i++)
        {
            bitmap.setPixel(i,y,0xffffffff);
        }
    }

    public static HashMap<String, Integer> analyze(Bitmap bitmap)
    {
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();

        int yellowPIxelCount = 0;
        int brownPixelCount = 0;
        int greenPixelCount = 0;

        for (int i = 0; i < x - 1; i++) {
            for (int j = 1; j < y - 1; j++) {
                int pixel = bitmap.getPixel(i,j);
                if(isYellowPixel(pixel))
                {
                    yellowPIxelCount++;
                }
                else if(isRedPixel(pixel))
                {
                    brownPixelCount++;
                }
                else if(isGreenPixel(pixel))
                {
                    greenPixelCount++;
                }
            }
        }
        HashMap<String,Integer> dataMap = new HashMap<>();
        dataMap.put(BROWN_COUNT,brownPixelCount);
        dataMap.put(YELLOW_COUNT,yellowPIxelCount);
        dataMap.put(GREEN_COUNT,greenPixelCount);

        return dataMap;
    }

    private static boolean isBrownPixel(int pixel)
    {
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = (pixel) & 0xff;
        return (r>100) && ((r-g)>40) && ((g-b)>40);
    }

    private static boolean isYellowPixel(int pixel)
    {
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = (pixel) & 0xff;
        return (r>250) && (g>240) && (b<150);
    }

}
