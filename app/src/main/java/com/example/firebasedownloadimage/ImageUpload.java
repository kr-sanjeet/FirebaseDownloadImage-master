package com.example.firebasedownloadimage;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class ImageUpload extends AppCompatActivity {
    private Button selectimg,btnUpload;
    private ImageView simg;
    private EditText etName;
    private static final int PICK_IMAGE_REQUEST=1;
    private DatabaseReference mdataref;
    private StorageReference mstorageref;
    final int IMAGE_REQUEST_CODE = 999;
    private ProgressBar progressBar;
    private Uri mimguri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);
        selectimg=(Button)findViewById(R.id.btn_select);
        simg=(ImageView)findViewById(R.id.selekedimg);
        btnUpload=(Button)findViewById(R.id.btn_upload);
        etName=(EditText)findViewById(R.id.img_name);
        progressBar=(ProgressBar)findViewById(R.id.progressbar);
        mstorageref= FirebaseStorage.getInstance().getReference("Uploads");
        mdataref= FirebaseDatabase.getInstance().getReference("Uploads");
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        selectimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ActivityCompat.requestPermissions(ImageUpload.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},IMAGE_REQUEST_CODE);
                Intent intent=new Intent(new Intent(Intent.ACTION_PICK));
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,"select image"),IMAGE_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==IMAGE_REQUEST_CODE){
            if (grantResults.length>0&& grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Intent intent=new Intent(new Intent(Intent.ACTION_PICK));
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,"select image"),IMAGE_REQUEST_CODE);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            mimguri = data.getData();
            Picasso.with(this).load(mimguri).into(simg);
        }
    }
    private String getFileExtensoin (Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap map = MimeTypeMap.getSingleton();
        return map.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void uploadImage() {
        if (mimguri!=null){
            final StorageReference storageReference=mstorageref.child(System.currentTimeMillis()+"."+ getFileExtensoin(mimguri));


            storageReference.putFile(mimguri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler=new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                }
                            },5000);
                            Toast.makeText(ImageUpload.this,"Upload SuccsessFul",Toast.LENGTH_SHORT).show();

                            List_data list_data=new List_data(etName.getText().toString().trim(),taskSnapshot.getMetadata().getReference().getPath());
                            Log.i("DOWNLOAD URL OF IMAGE ",taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                            String uploadid=mdataref.push().getKey();
                            mdataref.child(uploadid).setValue(list_data);
                            startActivity(new Intent(ImageUpload.this,MainActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double pr=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int) pr);
                }
            });
        }else {
            Toast.makeText(ImageUpload.this,"File Not Selected",Toast.LENGTH_SHORT).show();
        }
    }
}
