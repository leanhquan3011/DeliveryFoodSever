package com.leanhquan.deliveryfoodserver.Common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.leanhquan.deliveryfoodserver.API.IGeoCoordinates;
import com.leanhquan.deliveryfoodserver.API.RetrofitInit;
import com.leanhquan.deliveryfoodserver.Model.Request;
import com.leanhquan.deliveryfoodserver.Model.User;

public class Common {
    public static User currentUser;
    public static Request currentRequest;

    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final String baseURL= "https://fcm.googleapis.com/";

    public static String convertCodeToStatus(String code) {
        if(code.equals("0"))
            return"Đã đặt!";
        else if(code.equals("1"))
            return"Đang giao";
        else if(code.equals("2"))
            return"Đã hoàn thành";
        else {return "";}
    }

    public static IGeoCoordinates getIGeoService(){
        return RetrofitInit.getInstance(baseURL).create(IGeoCoordinates.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap scaleBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth/(float)bitmap.getWidth();
        float scaley = newHeight/(float)bitmap.getHeight();
        float pitvoX = 0, pivoY = 0;

        Matrix scaleMatrix = new Matrix();

        scaleMatrix.setScale(scaleX, scaley, pitvoX, pivoY);

        Canvas canvas = new Canvas(bitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0 , new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaleBitmap;

    }
}
