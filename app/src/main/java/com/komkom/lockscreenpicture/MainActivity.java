package com.komkom.lockscreenpicture;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener{

    private Button unlock, setting;
    private ImageView keyImage;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;

    private SurfaceView surfaceView;

    private boolean isSetting = false;

    private ArrayList<Point> oldPoints = new ArrayList<Point>(); //[0: [x, y],1: [x, y]]
    private ArrayList<Point> points = new ArrayList<Point>(); //[0: [x, y],1: [x, y]]

    private int round = 0;
    private int semiRound = 0; // need user to enter 2 times
    private final int MIN_ROUND = 3;
    private final int MIN_SEMIROUND = 1;
    private final String POINTSKEY_KEYNAME = "PointsKey";
    private static final int IMAGE_GALLERY_REQUEST = 20;
    private static final int RESULT_ENABLE = 11;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        makeFullScreen();
        startService(new Intent(this,LockScreenService.class));
        setContentView(R.layout.activity_main);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        registerAllView();

    }

    public void registerAllView() {
        unlock = (Button) findViewById(R.id.lock);
        unlock.setOnClickListener(this);
        setting = (Button) findViewById(R.id.setting);
        setting.setOnClickListener(this);
        keyImage = (ImageView) findViewById(R.id.keyImage);
        keyImage.setOnTouchListener(this);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
    }

    public void makeFullScreen() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(Build.VERSION.SDK_INT < 19) { //View.SYSTEM_UI_FLAG_IMMERSIVE is only on API 19+
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isActive = devicePolicyManager.isAdminActive(compName);
        // disable.setVisibility(isActive ? View.VISIBLE : View.GONE);
        // enable.setVisibility(isActive ? View.GONE : View.VISIBLE);
    }

    // get touch location
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == keyImage) {
            int[] viewCoords = new int[2];
            keyImage.getLocationOnScreen(viewCoords);

            int touchX = (int) event.getX();
            int touchY = (int) event.getY();


            int imageX = new Integer(touchX - viewCoords[0]); // viewCoords[0] is the X coordinate
            int imageY = new Integer(touchY - viewCoords[1]); // viewCoords[1] is the y coordinate

            Point curPoints  = new Point(imageX, imageY);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    points.add(curPoints);
                    Log.i("KOMKOM DEBUG","round : " + round);
                    round++;
                    if (isSetting) {
                        if (round < MIN_ROUND) {
                            // ask for more point or user cancel
                            // cancel in onClick
                        } else {
                            // save
                            setting.setText("Save");
                        }
                    } else {
                        // check if points == pointskey
                        ArrayList<Point> pointsKey = getPointsKeySetting();
                        if (pointsKey.size() == points.size()) {
                            if (Point.isNear(pointsKey, points)) {
                                // unlock !!
                                this.finish();
                            } else {
                                // password not correct
                                Toast.makeText(MainActivity.this, "Pattern not corrected", Toast.LENGTH_SHORT).show();
                            }
                            clearPoints();
                        }
                    }


                    Log.i("KOMKOM DEBUG", "X: " + curPoints.getX() + " Y: " + curPoints.getY()  + " event : " + event.getAction());
            }
            return true;
        }
        return false;
    }

    public void clearPoints() {
        points.clear();
        round = 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // if we are here, everything processed successfully.
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                // if we are here, we are hearing back from the image gallery.

                // the address of the image on the SD Card.
                Uri imageUri = data.getData();

                // declare a stream to read the image data from the SD Card.
                InputStream inputStream;

                // we are getting an input stream, based on the URI of the image.
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);

                    // get a bitmap from the stream.
                    Bitmap image = BitmapFactory.decodeStream(inputStream);


                    // show the image to the user
                    keyImage.setImageBitmap(image);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    // show a message to the user indictating that the image is unavailable.
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public ArrayList<Point> getPointsKeySetting() {
        TinyDB tinydb = new TinyDB(this);

        ArrayList<Object> tinyPointsKey = tinydb.getObject(POINTSKEY_KEYNAME, ArrayList.class);
        ArrayList<Point> pointsKey = new ArrayList<Point>();
        Gson gson = new Gson();
        Object[] arr = tinyPointsKey.toArray();
        for (int i = 0 ; i < tinyPointsKey.size() ; i++) {
            Point pointKey = gson.fromJson(arr[i].toString(), Point.class);
            pointsKey.add(pointKey);
        }
        return pointsKey;
    }

    public void saveSetting(ArrayList<Point> points) {

        TinyDB tinydb = new TinyDB(this);
        tinydb.putObject(POINTSKEY_KEYNAME, points);

        Log.i("KOMKOM DEBUG", "pointsObj : " + points);

        clearPoints();
        semiRound = 0;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK)
        {
            Log.i("KOMKOM DEBUG","BUTTON Clicked");
        }
        return false;
    }

    // override third button
    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    public void onClick(View view) {
        if (view == unlock) {
            // invoke the image gallery using an implict intent.
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

            // where do we want to find the data?
            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String pictureDirectoryPath = pictureDirectory.getPath();
            // finally, get a URI representation
            Uri data = Uri.parse(pictureDirectoryPath);

            // set the data and type.  Get all image types.
            photoPickerIntent.setDataAndType(data, "image/*");

            // we will invoke this activity, and get something back from it.
            startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
            // this.finish();
        } else if (view == setting) {
            if (isSetting) {
                if (round < MIN_ROUND) {
                    // cancel
                    isSetting = false;
                    Toast.makeText(MainActivity.this, "Canceling setting", Toast.LENGTH_SHORT).show();
                    setting.setText("Setting");
                } else {
                    if (semiRound < MIN_SEMIROUND) {
                        // ask for more
                        oldPoints.addAll(points);
                        Toast.makeText(MainActivity.this, "Please enter them again", Toast.LENGTH_SHORT).show();
                        semiRound++;
                        setting.setText("Cancel");
                    } else {
                        semiRound = 0;
                        // check if old points and new points is near
                        if (Point.isNear(oldPoints, points)) {
                            // save
                            saveSetting(points);
                            Toast.makeText(MainActivity.this, "Saved setting", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Pattern are not same. Canceling ..", Toast.LENGTH_SHORT).show();
                        }
                        isSetting = false;
                        setting.setText("Setting");
                    }
                }
                clearPoints();
            } else {
                // setting off
                isSetting = true;
                oldPoints.clear();
                setting.setText("Cancel");
                Toast.makeText(MainActivity.this, "Please touch atleast "+ MIN_ROUND + " point on the picture", Toast.LENGTH_SHORT).show();
            }

        }
    }
}