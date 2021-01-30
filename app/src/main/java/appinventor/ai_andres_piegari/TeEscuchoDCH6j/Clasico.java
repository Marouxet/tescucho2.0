package appinventor.ai_andres_piegari.TeEscuchoDCH6j;

//Librerias importadas (muchas se fueron agregando al ir copiando cosas de Google y Youtube)
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

//Clase Clasico es la app, AppCompactActivity es un tipo de App que tiene Android

public class Clasico extends AppCompatActivity {

    //Definiciones: Variables y todos los objetos existentes en la parte gráfica

    TextView resultView;
    TextView textoBarra;
    SeekBar sB;
    FloatingActionButton botonEscuchar;
    FloatingActionButton botoRojo;
    ImageView imagenLogo;
    RelativeLayout abajo;
    static Activity actividad;
    SpeechRecognizer speechRecognizer;
    Intent speechRecognizerIntent;

    private Toast toast;
    private long lastBackPressTime = 0;

    int seekValue;
    public String TextoCompleto = " ";
    static Boolean clasicoActiva = false;

    // Creación de la barra superior, para que aparezca la ruedita de configuración
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu:
                openNewActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    // Acá se le asigna a la ruedita para que abra la Actividad "Configuración"
    private void openNewActivity() {
        //Hago true la variable clasicoACtiva para saber que entré a configuración desde clásico y no desde continuo
        actividad = this;
        clasicoActiva = true;
        Intent intent = new Intent(Clasico.this, Configuracion.class);
        startActivity(intent);
    }

    //Adentró de este método, "onCreate" se pone todo lo que se produce durante la creación de la actividad. Es decir cuando se crea la pantalla.

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);

        //Elegir tema (Colores)
        //En SharedPReference se guardan las cosas que el usuario elije en la pestaña configuración. Las tomo y defino si el tema
        //es modo claro o moso oscuro

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        boolean modoOscuro = sharedPreferences.getBoolean("ModoOscuro", false);


        if (modoOscuro){
            this.setTheme(R.style.TemaOscuro);}
        else{
            this.setTheme(R.style.TemaClaro);

        }


        //Acá se toma como pantalla lo que está configurado en el archivo clasico.xml

        setContentView(R.layout.clasico);

        //En el caso de que el modo sea claro, le pongo fondo gris a la parte inferior.
        if (!modoOscuro) {
            abajo = findViewById(R.id.abajo_marco);
            abajo.setBackgroundColor(getResources().getColor(R.color.fondo_modoclaro))
            ;
        }


        //Configuración del texto de abajo de la barra. En caso de que sea modo claro, le pongo color blanco
        textoBarra= findViewById(R.id.texto_barra2);
        textoBarra.setText("Deslice la barra para cambiar el tamaño de la letra");
        if (!modoOscuro) {
            textoBarra.setTextColor(getResources().getColor(R.color.blanco));
            ;
        }

        // Cargo texto principal
        resultView = findViewById(R.id.result_text2);
        resultView.setText("Presione en la oreja para comenzar a hablar");

        // Le asigno a la barra desplegable que modifique el tamaño del texto
        sB = findViewById(R.id.seekBar2);
        sB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                seekValue = 3*i+30; //Calibración a ojo del tamaño de letra para que vaya de 20 a 60
                resultView.setTextSize(seekValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                resultView.setTextSize(seekValue);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                resultView.setTextSize(seekValue);
            }
        });

        // Le asigno al botón de la izqueirda que cierre tescucho secuencial y que abra  tescucuho continuo
        FloatingActionButton fab = findViewById(R.id.ir_a_continuo);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(v.getContext(), KaldiActivity.class);
                finish();
                startActivityForResult(intent2, 0);
            }
        });

        //Le asigno a los botones  de las orejas un nombre.
        botonEscuchar = findViewById(R.id.recognize_mic);
        botoRojo = findViewById(R.id.recognize_mic_rojo);

        /* Configuración del reconocimiento de voz de google -  Todo copiado de youtube */

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(Clasico.this);
        speechRecognizerIntent =  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            //Al principio del reconocimiento borro el texto principal
            public void onBeginningOfSpeech() {
                resultView.setText("");
                           }

            @Override
            public void onRmsChanged(float v) {
                Log.d("ACA", "onRmsChanged: ");

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                Log.d("ACA", "onEndOfSpeech: ACA");
            }

            @Override
            public void onError(int i) {
                resultView.setText("No se ha escuchado ningún mensaje");
                Vibrator vibrator3 = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                botoRojo.setVisibility(View.INVISIBLE);
                botonEscuchar.setVisibility(View.VISIBLE);
                vibrator3.vibrate(150);
            }

            @Override
            //Método ejecutado cuando se termina de escuchar. Se hace vibrar al teléfono y se cambian los íconos
            public void onResults(Bundle bundle) {
                Vibrator vibrator3 = (Vibrator) getSystemService(VIBRATOR_SERVICE);

                botoRojo.setVisibility(View.INVISIBLE);
                botonEscuchar.setVisibility(View.VISIBLE);
                vibrator3.vibrate(150);
            }

            @Override
            // Método ejecutado cuando hay resultados parciales. Tomo los resultados y los pongo en la pantalla principal
            public void onPartialResults(Bundle bundle) {
                ArrayList<String> texto = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(texto != null){
                    resultView.setText(texto.get(0).toUpperCase());
                }
            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });




    }

    @Override
    public void onStart() {
        super.onStart();
        clasicoActiva = false;
    }



    public void grabarClasico(View view){
        // Función que se activa cuando se presiona el botón de la oreja. La asignación a esta función está hecha en el archivo Clasico.xlm
        // Se hace vibrar el teléfono, se comienza a escuchar y se cambian los botones.
        Vibrator vibrator2 = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator2.vibrate(150);
        botonEscuchar.setVisibility(View.INVISIBLE);
        botoRojo.setVisibility(View.VISIBLE);
        resultView.setMovementMethod(new ScrollingMovementMethod());
        speechRecognizer.startListening(speechRecognizerIntent);

    }



    @Override
    // Método llamado cuando se presiona el botón atrás.
    // Se muestra un "toast" (mensaje flotante)
    // Si se presiona atrás de nuevo se cierra la app
    public void onBackPressed() {
        if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
            toast = Toast.makeText(this, "Tescucho es una app desarrollada por ingenieros del CISTAS UNTREF.  \n Universidad Nacional de Tres de Febrero. \n Todos los derechos reservados - 2020. \n Presione nuevamente \" atrás \" para salir.", Toast.LENGTH_LONG);
            toast.show();
            this.lastBackPressTime = System.currentTimeMillis();
        } else {
            if (toast != null) {
                toast.cancel();
            }
            super.onBackPressed();
        }
    }

}

