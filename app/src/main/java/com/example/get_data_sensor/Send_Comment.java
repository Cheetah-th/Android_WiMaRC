package com.example.get_data_sensor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Send_Comment extends AppCompatActivity {

    Button btnSelectCamera, btnSelectGallery, btnSend, btnDetectImage, btnClassifyImage;
    ImageView imageView;
    EditText txtMessage1, txtMessage2, txtMessage3, txtMessage4, txtMessage5, imageName;

    private String dbname;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    Bitmap bitmap = null;

    ProgressDialog progressDialog;
    String GetImageComment;

    String ServerUploadPath = "http://203.185.137.241/wimarctest/android_PHP/save_comment.php";

    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final int GALLERY_REQUEST_CODE = 105;

    //Name of the file that is saved by the camera
    String currentPhotoPath;

    private static final String MODEL_PATH = "optimized_graph.lite";
    private static final String LABEL_PATH = "retrained_labels.txt";
    private static final int INPUT_SIZE = 224;
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    private static final String TAG = "Send_Comment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_comment);

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
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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
                Toast.makeText(Send_Comment.this, "Logout successfully", Toast.LENGTH_LONG).show();
                startActivity(intent1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtMessage1 = findViewById(R.id.txtMessage1);
        txtMessage2 = findViewById(R.id.txtMessage2);
        txtMessage3 = findViewById(R.id.txtMessage3);
        txtMessage4 = findViewById(R.id.txtMessage4);
        txtMessage5 = findViewById(R.id.txtMessage5);
        imageView = findViewById(R.id.imageView);
        imageName = findViewById(R.id.editTextImageName);
        btnSelectCamera = findViewById(R.id.btnSelectCamera);
        btnSelectGallery = findViewById(R.id.btnSelectGallery);
        btnSend = findViewById(R.id.btnSend);
        btnDetectImage = findViewById(R.id.btnDetectImage);
        btnClassifyImage = findViewById(R.id.btnClassifyImage);
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
                    Toast.makeText(Send_Comment.this, "Please select image", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnClassifyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap != null) {
                    classifyFromImage();
                } else {
                    Toast.makeText(Send_Comment.this, "Please select image", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap != null) {
                    progressDialog = ProgressDialog.show(Send_Comment.this,"Sending comment","Please wait",false,false);
                    GetImageComment = imageName.getText().toString();
                    sendComment(ServerUploadPath);
                } else {
                    Toast.makeText(Send_Comment.this, "Please select image", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getData() {
        b = txtMessage1.getText().toString();
        c = txtMessage2.getText().toString();
        d = txtMessage3.getText().toString();
        e = txtMessage4.getText().toString();
        f = txtMessage5.getText().toString();
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
            Toast.makeText(this, "Image has been saved to " + currentPhotoPath, Toast.LENGTH_SHORT).show();
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

    private void sendComment(@NonNull final String url) {
        //Encode data
        getData();
        bitmap = reduceBitmapSize(bitmap, 921600);
        ByteArrayOutputStream byteArrayOutputStreamObject;
        byteArrayOutputStreamObject = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObject);
        byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();
        String ConvertImage = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);

        Ion.with(Send_Comment.this)
                .load(url)
                .setBodyParameter("A", dbname)
                .setBodyParameter("B", b)
                .setBodyParameter("C", c)
                .setBodyParameter("D", d)
                .setBodyParameter("E", e)
                .setBodyParameter("F", f)
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
                            Toast.makeText(Send_Comment.this, result, Toast.LENGTH_LONG).show();
                            clearData();

                        } else {
                            Toast.makeText(Send_Comment.this, "Error: " + e, Toast.LENGTH_LONG).show();
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

        TextRecognizer recognizer = new TextRecognizer.Builder(Send_Comment.this).build();
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<TextBlock> sparseArray =  recognizer.detect(frame);
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < sparseArray.size(); i++) {
            TextBlock tx = sparseArray.get(i);
            String str = tx.getValue();
            stringBuilder.append(str);
        }

        imageName.setText(stringBuilder);
    }

    private void classifyFromImage() {
        Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmapScaled);
        imageName.setText(results.toString());
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

    private void clearData() {
        txtMessage1.setText(null);
        txtMessage2.setText(null);
        txtMessage3.setText(null);
        txtMessage4.setText(null);
        txtMessage5.setText(null);
        imageView.setImageResource(android.R.color.transparent);
        imageName.setText(null);
        bitmap = null;
    }
}