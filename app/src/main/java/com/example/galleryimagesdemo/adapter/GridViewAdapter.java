package com.example.galleryimagesdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.galleryimagesdemo.R;
import com.example.galleryimagesdemo.activities.DisplayImageActivity;
import com.example.galleryimagesdemo.model.Model_images;

import java.util.ArrayList;


public class GridViewAdapter extends ArrayAdapter<Model_images> {

    Context context;
    ViewHolder viewHolder;
    ArrayList<Model_images> al_menu = new ArrayList<>();
    int int_position;

    public GridViewAdapter(Context context, ArrayList<Model_images> al_menu, int int_position) {
        super(context, R.layout.adapter_photosfolder, al_menu);
        this.al_menu = al_menu;
        this.context = context;
        this.int_position = int_position;
    }

    @Override
    public int getCount() {

        Log.e("ADAPTER LIST SIZE", al_menu.get(int_position).getAl_imagepath().size() + "");
        return al_menu.get(int_position).getAl_imagepath().size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        if (al_menu.get(int_position).getAl_imagepath().size() > 0) {
            return al_menu.get(int_position).getAl_imagepath().size();
        } else {
            return 1;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {

            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_photosfolder, parent, false);
            viewHolder.tv_foldern = (TextView) convertView.findViewById(R.id.tv_folder);
            viewHolder.tv_foldersize = (TextView) convertView.findViewById(R.id.tv_folder2);
            viewHolder.iv_image = (ImageView) convertView.findViewById(R.id.iv_image);
            viewHolder.synced = (TextView) convertView.findViewById(R.id.synced);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tv_foldern.setVisibility(View.GONE);
        viewHolder.tv_foldersize.setVisibility(View.GONE);

        if (al_menu.get(int_position).getAl_imagepath().get(position).isSynced()) {
            viewHolder.synced.setVisibility(View.VISIBLE);
        }

        try {
            Glide.with(context).load(al_menu.get(int_position).getAl_imagepath().get(position).getImagePath())

                    .into(viewHolder.iv_image);
            Log.i("IsSynced", String.valueOf(al_menu.get(int_position).getAl_imagepath().get(position).isSynced()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        viewHolder.iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DisplayImageActivity.class);
                intent.putExtra("Display_Image", al_menu.get(int_position).getAl_imagepath().get(position).getImagePath());
                context.startActivity(intent);
            }
        });

        return convertView;

    }

    private static class ViewHolder {
        TextView tv_foldern, tv_foldersize, synced;
        ImageView iv_image;
    }
}
