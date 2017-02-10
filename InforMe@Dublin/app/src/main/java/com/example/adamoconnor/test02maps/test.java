package com.example.adamoconnor.test02maps;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Adam O'Connor on 02/02/2017.
 */

public class test extends AppCompatActivity {

    //Declaring our ImageView
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.database);

        //Initializing the ImageView
        imageView = (ImageView) findViewById(R.id.imageView);

        //Loading Image from URL
        Picasso.with(this)
                .load("https://www.simplifiedcoding.net/wp-content/uploads/2015/10/advertise.png")
                .placeholder(R.drawable.error)   // optional
                .error(R.drawable.error)      // optional
                .resize(400,400)                        // optional
                .into(imageView);
    }
}
