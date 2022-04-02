package com.example.walkingtours;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class FenceInfoActivity extends AppCompatActivity {

    ImageView image;
    private Picasso picasso;
    private Typeface myCustomFont;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence_info);
        String font = "fonts/Acme-Regular.ttf";
        myCustomFont = Typeface.createFromAsset(getAssets(), font);
        String title = getIntent().getStringExtra("ID");
        String address = getIntent().getStringExtra("Address");
        String url = getIntent().getStringExtra("Url");
        String desc = getIntent().getStringExtra("Desc");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("");
        actionBar.setIcon(R.drawable.home_image);
        image = findViewById(R.id.fenceImage);
        picasso = Picasso.get();
        loadRemoteImage(url);
        TextView titleView = findViewById(R.id.fenceTitle);
        titleView.setText(title);
        titleView.setTextColor(Color.WHITE);
        titleView.setTypeface(myCustomFont);
        TextView addressView = findViewById(R.id.fenceAddress);
        addressView.setText(address);
        addressView.setTextColor(Color.WHITE);
        addressView.setTypeface(myCustomFont);

        TextView descView = findViewById(R.id.fenceInfo);
        descView.setMovementMethod(new ScrollingMovementMethod());
        descView.setText(desc);
        descView.setTextColor(Color.parseColor("#006400"));
        descView.setTypeface(myCustomFont);




    }

    public void loadRemoteImage (String url) {

        if (!url.isEmpty()) {
            picasso.load(url)
                    .error(R.drawable.missing)
                    .placeholder(R.drawable.placeholder)
                    .into(image);
        }
        else{
            image.setImageResource(R.drawable.brokenimage);

        }
    }
}