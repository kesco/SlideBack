package com.kesco.adk.moko.slideback.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kesco.adk.moko.slideback.SlideEdge;
import com.kesco.adk.moko.slideback.SlideShadow;
import com.kesco.adk.moko.slideback.Slider;

public class CommonSlideActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_slide);
        Intent intent = getIntent();
        int type = intent.getIntExtra("slide_type", 1);
        SlideEdge edge;
        switch (type) {
            case 1:
                edge = SlideEdge.LEFT;
                break;
            case 2:
                edge = SlideEdge.RIGHT;
                break;
            case 3:
                edge = SlideEdge.TOP;
                break;
            case 4:
                edge = SlideEdge.BOTTOM;
                break;
            default:
                edge = SlideEdge.LEFT;
                break;
        }

        Slider.INSTANCE$.attachToScreen(this, edge, SlideShadow.FULL);

        findViewById(R.id.btn_left_slide).setOnClickListener(this);
        findViewById(R.id.btn_right_slide).setOnClickListener(this);
        findViewById(R.id.btn_top_slide).setOnClickListener(this);
        findViewById(R.id.btn_bottom_slide).setOnClickListener(this);
    }

    @Override
    public void onClick(@NonNull View v) {
        Intent intent = new Intent(CommonSlideActivity.this, CommonSlideActivity.class);
        switch (v.getId()) {
            case R.id.btn_left_slide:
                intent.putExtra("slide_type", 1);
                break;
            case R.id.btn_right_slide:
                intent.putExtra("slide_type", 2);
                break;
            case R.id.btn_top_slide:
                intent.putExtra("slide_type", 3);
                break;
            case R.id.btn_bottom_slide:
                intent.putExtra("slide_type", 4);
                break;
        }
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_slide_left, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
