package com.komkom.lockscreenpicture;

import android.util.Log;

import java.util.ArrayList;

public class Point {

    private final static  int SENSITIVE = 70;

    private int x, y;

    public Point(int x, int y){
        this.setX(x);
        this.setY(y);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setX(int x){
        this.x = x;
        return;
    }

    public void setY(int y){
        this.y = y;
        return;
    }

    public boolean isNear(Point point) {
        // logic check is x point is near y point here
        boolean isNearX = false, isNearY = false;
        Log.i("KOMKOM DEBUG", "SENSITIVE : " + SENSITIVE );
        if (this.getX() >= (point.getX() - SENSITIVE) && this.getX() <= (point.getX() + SENSITIVE)) {
                isNearX = true;
        }
        if (this.getY() >= (point.getY() - SENSITIVE) && this.getY() <= (point.getY() + SENSITIVE)) {
            isNearY = true;
        }
        Log.i("KOMKOM DEBUG", "point1 : " +this.getX() + " " + this.getY() );
        Log.i("KOMKOM DEBUG", "point2 : " +point.getX() + " " + point.getY() );
        Log.i("KOMKOM DEBUG", "isNearX : " + isNearX + " isNearY : " + isNearY );
        if (isNearX && isNearY) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNear(Point point1, Point point2) {
        // logic check is x point is near y point here
        boolean isNearX = false, isNearY = false;
        Log.i("KOMKOM DEBUG", "SENSITIVE : " + SENSITIVE );
        if (point1.getX() >= (point2.getX() - SENSITIVE) && point1.getX() <= (point2.getX() + SENSITIVE)) {
            isNearX = true;
        }
        if (point1.getY() >= (point2.getY() - SENSITIVE) && point1.getY() <= (point2.getY() + SENSITIVE)) {
            isNearY = true;
        }
        Log.i("KOMKOM DEBUG", "point1 : " +point1.getX() + " " + point1.getY() );
        Log.i("KOMKOM DEBUG", "point2 : " +point2.getX() + " " + point2.getY() );
        Log.i("KOMKOM DEBUG", "isNearX : " + isNearX + " isNearY : " + isNearY );
        if (isNearX && isNearY) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNear(ArrayList<Point> pointArrayList1, ArrayList<Point> pointArrayList2) {
        // logic check is x point is near y point here
        Log.i("KOMKOM DEBUG", "pointArrayList1 size : " +pointArrayList1.size() + " pointArrayList2 size : " + pointArrayList2.size() );
        if (pointArrayList1.size() != pointArrayList2.size()) {
            return false;
        }
        for (int i = 0 ; i < pointArrayList1.size() ; i++) {
            Log.i("KOMKOM DEBUG", "points set : " + (i + 1) );
            Point point1 = pointArrayList1.get(i);
            Point point2 = pointArrayList2.get(i);
            if (!(point1.isNear(point2))) {
                return false;
            }
        }
        return true;
    }

}
