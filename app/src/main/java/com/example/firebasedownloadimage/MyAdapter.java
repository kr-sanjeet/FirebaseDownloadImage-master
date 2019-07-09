package com.example.firebasedownloadimage;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<List_data>list_data;
    private Context ct;
    ProgressDialog progressDialog;
    StorageReference storage;StorageReference fileRef;
    Map<Integer,String> elementAddress = new HashMap<Integer, String>() ;

    public MyAdapter(List<List_data> list_data, Context ct) {
        this.list_data = list_data;
        this.ct = ct;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_data,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
         storage = FirebaseStorage.getInstance().getReference();
        final List_data ld=list_data.get(position);
        holder.tvname.setText(ld.getName());
         fileRef = storage.child(ld.getImgUrl());
        //Log.i("IMAGE URL IS HERE",ld.getImgUrl());

        elementAddress.put(position,ld.getImgUrl());
        final long ONE_MEGABYTE = 1024*1024*5;
        fileRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                holder.imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        storage.child(ld.getImgUrl()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
            //    Log.i("DOWNLOADING URL IS ",uri.getPath());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    @Override
    public int getItemCount() {
        return list_data.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView tvname;
        private Button download_btn;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView=(ImageView)itemView.findViewById(R.id.img_view);
            tvname=(TextView)itemView.findViewById(R.id.uName);
            download_btn = itemView.findViewById(R.id.download_pic_btn);
            //download_btn.setTag(123,itemView);

            download_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("ITEM CLICKED",Integer.toString(getAdapterPosition()));
                    StorageReference storageReference = storage.child(elementAddress.get(getAdapterPosition()));

                    StorageReference childReference = storage.child(storageReference.getPath());
                    long ONE_MEGABYTE = 1024*1024*5;
                    Log.i("STORAGE REFERENCES ",elementAddress.get(getAdapterPosition()));
//                   
                    File rootPath = new File(Environment.getExternalStorageDirectory(),"Downloads");
                    if(!rootPath.exists()){
                        rootPath.mkdirs();
                    }

                    final File localFile = new File(rootPath,"photo.jpg");

                    childReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ct.getApplicationContext(),"DOWNLOAD SUCCESSFULLY",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ct.getApplicationContext(),"ERROR WHILE DOWNLOADING",Toast.LENGTH_SHORT).show();
                       Log.i("DOWNLOADING EXCEPTION ",e.getMessage());
                        }
                    });
                }
            });
        }
    }
}