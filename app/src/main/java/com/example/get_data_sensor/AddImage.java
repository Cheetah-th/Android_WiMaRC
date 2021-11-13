package com.example.get_data_sensor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class AddImage extends AppCompatActivity {

    Button btnSelectCamera, btnSelectGallery, btnUploadImage, btnDetectImage, btnClassifyImage;
    ImageView imageView;
    EditText imageName;
    TextView textPath, textFromImage;

    private String dbname;
    Bitmap bitmap = null;

    ProgressDialog progressDialog;
    String GetImageComment;

    String ServerUploadPath = "http://203.185.137.241/wimarctest/add-image.php";

    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final int GALLERY_REQUEST_CODE = 105;

    //Name of the file that is saved by the camera
    String currentPhotoPath;

    private static final String MODEL_PATH = "optimized_graph.lite";
    private static final String LABEL_PATH = "retrained_labels.txt";
    private static final int INPUT_SIZE = 224;
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);

        dbname = getIntent().getExtras().getString("username");

        try {
            classifier = TensorFlowImageClassifier.create(getAssets(), MODEL_PATH, LABEL_PATH, INPUT_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        initViews();
        initEvent();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(getApplicationContext(), AddComment.class);
            intent.putExtra("username", dbname);
            finish();
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.main_page:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("username", dbname);
                finish();
                startActivity(intent);
                return true;

            case R.id.logout:
                Intent intent1 = new Intent(getApplicationContext(), Login.class);
                finish();
                Toast.makeText(AddImage.this, "Logout successfully", Toast.LENGTH_LONG).show();
                startActivity(intent1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.imageView);
        imageName = findViewById(R.id.editTextImageName);
        btnSelectCamera = findViewById(R.id.btnSelectCamera);
        btnSelectGallery = findViewById(R.id.btnSelectGallery);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnDetectImage = findViewById(R.id.btnDetectImage);
        btnClassifyImage = findViewById(R.id.btnClassifyImage);
        textPath = findViewById(R.id.textPath);
        textFromImage = findViewById(R.id.textFromImage);
    }

    private void initEvent() {
        btnSelectCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        btnSelectGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });

        btnDetectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap != null) {
                    detectTextFromImage();
                } else {
                    Toast.makeText(AddImage.this, "Please select image", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnClassifyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap != null) {
                    classifyFromImage();
                } else {
                    Toast.makeText(AddImage.this, "Please select image", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap != null) {
                    progressDialog = ProgressDialog.show(AddImage.this,"Image is uploading","Please wait",false,false);
                    GetImageComment = imageName.getText().toString();
                    uploadImageToServer(ServerUploadPath);
                } else {
                    Toast.makeText(AddImage.this, "Please select image", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Something went wrong. Could not create file.", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.sensor.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

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
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            bitmap = (Bitmap) BitmapFactory.decodeFile(currentPhotoPath);
            imageView.setImageBitmap(bitmap);
            textPath.setText(currentPhotoPath);
        }
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToServer(@NonNull final String url) {
        //Encode data
        bitmap = reduceBitmapSize(bitmap, 921600);
        ByteArrayOutputStream byteArrayOutputStreamObject;
        byteArrayOutputStreamObject = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObject);
        byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();
        String ConvertImage = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);

        Ion.with(AddImage.this)
                .load(url)
                .setBodyParameter("image_comment", GetImageComment)
                .setBodyParameter("image_data", ConvertImage)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        progressDialog.dismiss();
                        Log.e(TAG, "Network error: " + e);
                        Log.w(TAG, "Result: " + result);

                        if (result != null) {
                            Toast.makeText(AddImage.this, result, Toast.LENGTH_LONG).show();
                            imageView.setImageResource(android.R.color.transparent);
                            imageName.setText(null);
                            textPath.setText(null);
                            bitmap = null;
                        } else {
                            Toast.makeText(AddImage.this, "Error: " + e, Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    private void detectTextFromImage() {
        /*
        //perform text detection here

        //TODO 1. define TextRecognizer
        TextRecognizer recognizer = new TextRecognizer.Builder(MainActivity.this).build();

        //TODO 2. Get bitmap from imageview
        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

        //TODO 3. get frame from bitmap
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();

        //TODO 4. get data from frame
        SparseArray<TextBlock> sparseArray =  recognizer.detect(frame);

        //TODO 5. set data on textview
        StringBuilder stringBuilder = new StringBuilder();

        for(int i=0;i < sparseArray.size(); i++){
            TextBlock tx = sparseArray.get(i);
            String str = tx.getValue();
            stringBuilder.append(str);
        */

        textFromImage.setText(null);
        TextRecognizer recognizer = new TextRecognizer.Builder(AddImage.this).build();
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<TextBlock> sparseArray =  recognizer.detect(frame);
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < sparseArray.size(); i++) {
            TextBlock tx = sparseArray.get(i);
            String str = tx.getValue();
            stringBuilder.append(str);
        }

        textFromImage.setText(stringBuilder);
    }

    private void classifyFromImage() {
        textFromImage.setText(null);
        Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmapScaled);
        textFromImage.setText(results.toString());
    }

    private static Bitmap reduceBitmapSize(Bitmap bitmap,int MAX_SIZE) {
        /* Equation
            bitmapHeight / ratio * bitmapWidth / ratio = MAX_SIZE
            -> find ratio

            Ex. 1920 / ratio * 1080 / ratio         = 921600      ; 921600 is youtube 720p hd
                2073600 / ratio^2                   = 921600
                ratio^2                             = 2073600 / 921600
                ratio^2                             = 2.25
                ratio                               = sqrt(2.25)
                ratio                               = 1.5
        */

        double ratioSquare;
        int bitmapHeight, bitmapWidth;
        bitmapHeight = bitmap.getHeight();
        bitmapWidth = bitmap.getWidth();
        ratioSquare = (bitmapHeight * bitmapWidth) / MAX_SIZE;
        if (ratioSquare <= 1) {
            return bitmap;
        }
        double ratio = Math.sqrt(ratioSquare);
        Log.d("mylog", "Ratio: " + ratio);
        int requiredHeight = (int) Math.round(bitmapHeight / ratio);
        int requiredWidth = (int) Math.round(bitmapWidth / ratio);
        return Bitmap.createScaledBitmap(bitmap, requiredWidth, requiredHeight, true);
    }
}