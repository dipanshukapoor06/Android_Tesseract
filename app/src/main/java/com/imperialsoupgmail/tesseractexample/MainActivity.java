package com.imperialsoupgmail.tesseractexample;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Bitmap image;
    private TessBaseAPI mTess;
    String datapath = "";
    Camera camera;
    FrameLayout frameLayout;
    ShowCamera showCamera;
    File picture_file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        camera = Camera.open();
        showCamera = new ShowCamera(this,camera);
        frameLayout.addView(showCamera);


    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            picture_file = null;
            try {
                picture_file = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Picture File..................................");
            System.out.println(picture_file);

            if(picture_file == null) {
                return;
            }
            else {
                try {
                    FileOutputStream fos = new FileOutputStream(picture_file);
                    fos.write(data);
                    fos.close();

                    camera.startPreview();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            image = BitmapFactory.decodeFile(currentPhotoPath);

            String language = "tam";
            datapath = getFilesDir()+ "/tesseract/";
            mTess = new TessBaseAPI();

            // checkFile(new File(datapath + "tessdata/"));

            mTess.init(datapath, language);
            image = rotateImage(image,90);
            String OCRresult = null;
            mTess.setImage(image);
            OCRresult = mTess.getUTF8Text();
            System.out.println("Result......................    " + OCRresult);
            Toast.makeText(getBaseContext(),OCRresult, Toast.LENGTH_SHORT).show();
            //TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
            // OCRTextView.setText(OCRresult);
        }
    };

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }



    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        System.out.println("Current photo path ..........................................");
        System.out.println(currentPhotoPath);
        return image;
    }

//    private File getOutputMediaFile() {
//        String state = Environment.getExternalStorageState();
//        if(!state.equals(Environment.MEDIA_MOUNTED)) {
//            return null;
//        }
//        else{
//            File folder_gui = new File(Environment.getExternalStorageDirectory() + File.separator + "GUI");
//            if(!folder_gui.exists()) {
//                folder_gui.mkdirs();
//                System.out.println("..........................................................................");
//                System.out.println(folder_gui.getAbsolutePath());
//                System.out.println(".............................................................................");
//            }
//          File outputFile = new File(folder_gui,"temp.jpg");
//            return outputFile;
//        }
//    }



    public void processImage(View view){

        if(camera!=null){
            camera.takePicture(null, null, mPictureCallback);
        }
//        image = BitmapFactory.decodeFile(currentPhotoPath);
//
//        String language = "tam";
//        datapath = getFilesDir()+ "/tesseract/";
//        mTess = new TessBaseAPI();
//
//        // checkFile(new File(datapath + "tessdata/"));
//
//        mTess.init(datapath, language);
//
//        String OCRresult = null;
//        mTess.setImage(image);
//        OCRresult = mTess.getUTF8Text();
//         Toast.makeText(getBaseContext(),OCRresult, Toast.LENGTH_SHORT).show();
//        TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
//        OCRTextView.setText(OCRresult);
    }

    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
                copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/tam.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/tam.traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/tam.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
