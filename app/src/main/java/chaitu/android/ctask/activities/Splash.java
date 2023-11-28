package chaitu.android.ctask.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import chaitu.android.ctask.R;

public class Splash extends AppCompatActivity {

    private static int DELAY_TIME = 4000;

    Animation topAnim, bottomAnim;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_anim);


        imageView = findViewById(R.id.image);
        imageView.setAnimation(topAnim);
        new Handler().postDelayed(() -> {

            Intent i = new Intent(Splash.this,MainActivity.class);
            startActivity(i);
            finish();

        },DELAY_TIME);
    }
}