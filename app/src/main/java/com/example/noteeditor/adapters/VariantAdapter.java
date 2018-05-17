package com.example.noteeditor.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.noteeditor.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class VariantAdapter extends BaseAdapter {
    private LayoutInflater lInflater;
    private ArrayList<Variant> objects;

    public VariantAdapter(Context context, ArrayList<Variant> notes) {
        objects = notes;
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.list_item_variant, parent, false);
        }

        final ImageView photoIV = view.findViewById(R.id.imageView_photo);
        photoIV.setImageResource(R.drawable.icons_camera);
        Variant v = getVariant(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.textVariantTitle)).setText(v.title);
        ((TextView) view.findViewById(R.id.textVariantLikes)).setText(String.valueOf(v.likes));
        ((TextView) view.findViewById(R.id.textVariantDislikes)).setText(String.valueOf(v.dislikes));

        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        final File localFile;
        try {
            localFile = File.createTempFile(v.noteID + "_" + v.variantID, "jpg");

            mStorageRef.child("variant_photo/" + v.noteID + "/" + v.variantID).getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            photoIV.setImageURI(Uri.fromFile(localFile));
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return view;
    }

    Variant getVariant(int position) {
        return ((Variant) getItem(position));
    }
}

