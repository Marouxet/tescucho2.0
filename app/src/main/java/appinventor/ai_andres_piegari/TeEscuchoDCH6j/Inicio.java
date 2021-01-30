package appinventor.ai_andres_piegari.TeEscuchoDCH6j;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import appinventor.ai_andres_piegari.TeEscuchoDCH6j.R;

import java.util.Timer;
import java.util.TimerTask;

public class Inicio extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(R.style.TemaInicio);
        setContentView(R.layout.inicio);

        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run(){
                finish();
                Intent intent =  new Intent(Inicio.this, KaldiActivity.class);
                startActivity(intent);


            }

        },5000);
    }

}