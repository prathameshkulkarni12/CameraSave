package com.example.camerasave;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.camerasave.Database.SQLiteHelper;
import com.example.camerasave.Models.ImageOrientation;
import com.example.camerasave.Models.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    final static private int NEW_PICTURE=1;
    private String cFileName;
    Button Capture,Show,BtnDelete,btnConvert;
    File op,dir;
    ImageView DisplayImg;
    Uri uri;
    TextView ConvertedTV;
    int id=1;
    String Path;
    String[] fileName;
    SQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Capture=findViewById(R.id.btnCapture);
        Show=findViewById(R.id.btnSave);
        DisplayImg = findViewById(R.id.DisplayImg);
        BtnDelete = findViewById(R.id.btnDelete);
        btnConvert = findViewById(R.id.btnConvert);
        ConvertedTV = findViewById(R.id.ConvertedBinary);

        sqLiteHelper=new SQLiteHelper(getApplicationContext());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, 1);

        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
         ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},23);
        }
        id =sqLiteHelper.getID();
        if (id == 0) {
            id = 1;
        } else {
            id = id + 1;
        }
        final Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        dir= new File(Environment.getExternalStorageDirectory() +
                File.separator +"Android"+File.separator+"data"+File.separator+ "TEST");
        op = new File(dir,id+".png");
        if (!dir.exists()){
            dir.mkdirs();
        }
        Capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(op));
                startActivityForResult(i,150);
            }
        });
        Show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id=sqLiteHelper.getID();
                Path=sqLiteHelper.getPath(id);
                if (Path!=null){
                    File fp= new File(Path);
                    if (fp.exists()){
//                        Bitmap bitmap=BitmapFactory.decodeFile(fp.getAbsolutePath());
                        Bitmap bitmap= null;
                        try {
                            bitmap = handleSamplingAndRotationBitmap(getApplicationContext(), Uri.fromFile(fp.getAbsoluteFile()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        DisplayImg.setImageBitmap(bitmap);
                    }
                }
            }
        });
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id=sqLiteHelper.getID();
                Path=sqLiteHelper.getPath(id);
//                byte[] Encoded=encodeImage(Path);
//                byte[] byteArr = Base64.decode(Encoded, Base64.DEFAULT);
//                ConvertedTV.setText(byteArr.toString());
//                System.out.println(Encoded);
                Bitmap bitmap=BitmapFactory.decodeFile(Path);
                byte[] bitmay= Util.getBytes(bitmap);
                System.out.println("BitmapByte : :"+ Arrays.toString(bitmay));

            }
        });

        BtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id=sqLiteHelper.getID();
                Path=sqLiteHelper.getPath(id);
                fileName=Path.split("/",10);
                File DFile=new File(Path);
                if (DFile.delete()){
                    sqLiteHelper.deleteEntry(id);
                    Toast.makeText(MainActivity.this, "Deleted :: "+Path, Toast.LENGTH_SHORT).show();
                }
            }
        });

        DisplayImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id=sqLiteHelper.getID();
                Path=sqLiteHelper.getPath(id);
                final Dialog builder = new Dialog(MainActivity.this);

                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                builder.setCancelable(false);
                builder.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //nothing;
                    }
                });
              uri=Uri.fromFile(new File(Path));
              builder.setContentView(R.layout.dialog_layout);
              Dialog dialog;
              ImageButton close=builder.findViewById(R.id.btnClose);
              getSupportActionBar().hide();
              ImageView img = builder.findViewById(R.id.Img);
              img.getLayoutParams().height=ViewGroup.LayoutParams.WRAP_CONTENT;
              img.getLayoutParams().width=ViewGroup.LayoutParams.WRAP_CONTENT;
              img.setAdjustViewBounds(false);

              img.setImageURI(uri);
              close.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      builder.dismiss();
                  }
              });
//                ImageView imageView = new ImageView(MainActivity.this);
//                imageView.setImageURI(uri);                //set the image in dialog popup
//                //below code fullfil the requirement of xml layout file for dialoge popup
//
//                builder.addContentView(imageView, new RelativeLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT));

                builder.show();
            }
        });
    }

    private byte[] encodeImage(String path)
    {
        File imagefile = new File(path);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(imagefile);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] b = baos.toByteArray();
        byte encImage[] = Base64.encode(b, Base64.DEFAULT);
        //Base64.de
        return encImage;

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 150) {
            if (resultCode == RESULT_OK) {
                if (op.exists()) {

                    Bitmap bitmap = BitmapFactory.decodeFile(op.getAbsolutePath());
                    System.out.println(op.getAbsolutePath());
                    sqLiteHelper.addEntry(id,op.getAbsolutePath());
//                    DisplayImg.setImageBitmap(bitmap);
                }
                else{
                    op.mkdirs();
                }
            }
        }
    }
    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

}
