package appinventor.ai_andres_piegari.TeEscuchoDCH6j;

//Librerias importadas (muchas se fueron agregando al ir copiando cosas de Google y Youtube) y muchas venían con el VOSK

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;



import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.kaldi.Assets;
import org.kaldi.KaldiRecognizer;
import org.kaldi.Model;
import org.kaldi.RecognitionListener;
import org.kaldi.SpeechRecognizer;
import org.kaldi.Vosk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;


//Clase Kaldiactiitty es la app, AppCompactActivity es un tipo de App que tiene Android
// En este caso dice implement RecognitionListener. Eso venía con el VOSK

public class KaldiActivity extends AppCompatActivity implements
        RecognitionListener {

    static {
        System.loadLibrary("kaldi_jni");
    }

    //Variables de estado que venían con el VOSK
    static private final int STATE_START = 0;
    static private final int STATE_READY = 1;
    static private final int STATE_DONE = 2;
    static private final int STATE_FILE = 3;
    static private final int STATE_MIC  = 4;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    //Definiciones: Variables y todos los objetos existentes en la parte gráfica

    private Model model;
    private SpeechRecognizer recognizer;
    TextView resultView;
    TextView textoBarra;
    FloatingActionButton recMic;
    FloatingActionButton shareText;
    SeekBar sB;
    RelativeLayout abajo;
    int seekValue;
    public String TextoCompleto = " ";
    public Boolean resetApp = false;
    public int statusUi = 0;
    private Toast toast;
    private long lastBackPressTime = 0;

    public static final  boolean MODO_OSCURO = false;
    static boolean continuoActiva = false;
    static Activity actividad;

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
        actividad = this;
        continuoActiva = true;
        Intent intent = new Intent(KaldiActivity.this, Configuracion.class);
        startActivity(intent);
    }




    //Adentró de este método, "onCreate" se pone todo lo que se produce durante la creación de la actividad. Es decir cuando se crea la pantalla.
    @Override
    public void onCreate(Bundle state) {

        super.onCreate(state);

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
        setContentView(R.layout.main);

        //En el caso de que el modo sea claro, le pongo fondo gris a la parte inferior.
        if (!modoOscuro) {
            abajo = findViewById(R.id.abajo_marco2);
            abajo.setBackgroundColor(getResources().getColor(R.color.fondo_modoclaro))
            ;
        }

        //Configuración del texto de abajo de la barra. En caso de que sea modo claro, le pongo color blanco
        textoBarra= findViewById(R.id.texto_barra);
        textoBarra.setText("Deslice la barra para cambiar el tamaño de la letra");

        if (!modoOscuro) {

            textoBarra.setTextColor(getResources().getColor(R.color.blanco));
            ;
        }

        // Cargo texto principal
        resultView = findViewById(R.id.result_text);

        // Le asigno a la barra desplegable que modifique el tamaño del texto
        sB = findViewById(R.id.seekBar);
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
        FloatingActionButton fab = findViewById(R.id.ir_a_clasico);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2) {
                Intent intent3 = new Intent(v2.getContext(), Clasico.class);
                finish();
                startActivityForResult(intent3, 0);
            }
        });

        // Le asigno al botón de arriba (escondido por defecto) la función de compartir el texto transcripto
        FloatingActionButton shareText = findViewById(R.id.compartir);
        shareText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, resultView.getText().toString());
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, "Texto transcripto por Tescucho @CISTAS");
                startActivity(shareIntent);
            }
        });


        // Le asigno al botón del micrófono la función de activar el sistema VOSK
        recMic = findViewById(R.id.recognize_mic);
        findViewById(R.id.recognize_mic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognizeMicrophone();
            }
        });

        //En el caso del modo continuo, tuve que hacer unas cosas para que cuando se gire el teléfono
        //no vueva  a empezar. Por eso está la variblae resetApp. Ahora lo que hago es cambiar la gráfica
        //Si se resetea la app, para que siga grabando.


        if(resetApp){
            recMic.setImageResource(R.drawable.ic_pause_24px);
            recMic.setEnabled(true);
        }
        else{
            recMic.setImageResource(R.drawable.oreja_negra);

        }


        if(statusUi==4){
        setUiState(STATE_MIC);
        }
        else{
            setUiState(STATE_START);
        }



        // Check if user has given permission to record audio

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        AppRate.with(this)

                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(4) // default 10
                .setRemindInterval(2) // default 1
                .setShowLaterButton(true) // default true

                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.

                    @Override
                    public void onClickButton(int which) {
                        Log.d(KaldiActivity.class.getName(), Integer.toString(which));
                    }
                })

                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);


        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new SetupTask(this).execute();

           }

    @Override
    public void onStart() {
        super.onStart();
        continuoActiva = false;
    }



// De acá en adelante vino con el VOSK.Yo lo fui modificando pero no entiendo el grueso. Solo comento
// aquello que cambié.

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<KaldiActivity> activityReference;

        SetupTask(KaldiActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                Log.d("KaldiDemo", "Sync files in the folder " + assetDir.toString());

                Vosk.SetLogLevel(0);

                activityReference.get().model = new Model(assetDir.toString() + "/model-android");
            } catch (IOException e) {
                return e;
            }
            if(activityReference.get().resetApp){

               activityReference.get().recognizeMicrophone();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                activityReference.get().setErrorState(String.format(activityReference.get().getString(R.string.failed), result));
            } else {
                activityReference.get().setUiState(STATE_READY);
            }
        }


    }

    private static class RecognizeTask extends AsyncTask<Void, Void, String> {
        WeakReference<KaldiActivity> activityReference;
        WeakReference<TextView> resultView;

        RecognizeTask(KaldiActivity activity, TextView resultView) {
            this.activityReference = new WeakReference<>(activity);
            this.resultView = new WeakReference<>(resultView);
        }

        @Override
        protected String doInBackground(Void... params) {
            KaldiRecognizer rec;
            long startTime = System.currentTimeMillis();
            StringBuilder result = new StringBuilder();
            try {
                rec = new KaldiRecognizer(activityReference.get().model, 16000.f, "oh zero one two three four five six seven eight nine");

                InputStream ais = activityReference.get().getAssets().open("10001-90210-01803.wav");
                if (ais.skip(44) != 44) {
                    return "";
                }
                byte[] b = new byte[4096];
                int nbytes;
                while ((nbytes = ais.read(b)) >= 0) {
                    if (rec.AcceptWaveform(b, nbytes)) {
                        result.append(rec.Result());
                    } else {
                        result.append(rec.PartialResult());
                    }
                }
                result.append(rec.FinalResult());
            } catch (IOException e) {
                return "";
            }
            return String.format(activityReference.get().getString(R.string.elapsed), result.toString(), (System.currentTimeMillis() - startTime));
        }

        @Override
        protected void onPostExecute(String result) {
            activityReference.get().setUiState(STATE_READY);
            resultView.get().append(result + "\n");
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                new SetupTask(this).execute();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }


    @Override
    //onResult se ejecuta cuando se termina de capturar texto. El sistema ruso va tirando resultados parciales
    // y cada tanto tira un global. Cuando tira el global se ejecuta este método.
    // lo que hice fue tomar el texto guardado en resultView y meterlo en la variable Texto Completo,para ir concatenando.
    public void onResult(String hypothesis) {
        TextoCompleto = resultView.getText().toString() +'\n';
    }

    @Override
    // onPartial result se ejecuta cuando hay resultados parciales.
    // El sistema ruso tiene un formato de salida que está en formato json. lo que hice fue manipular
    // el texto (eliminar caracteres y demás) para que sólo quede lo que reconoció.
    // Luego lo concateno con TextoCompleto (que tiene lo anterior)
    // Y lo muestro en resultView

    public void onPartialResult(String hypothesis) {
        String palabra3 = hypothesis.substring(17,hypothesis.length()-3);
        String palabra4 = TextoCompleto + palabra3 ;
        resultView.setText(palabra4.toUpperCase());

    }

    @Override
    public void onError(Exception e) {
        setErrorState(e.getMessage());
    }

    @Override
    public void onTimeout() {
        recognizer.cancel();
        recognizer = null;
        setUiState(STATE_READY);
    }

    // Está función venía con el VOSK. Los tipos definen cuatro estados para el sistema
    // START, READY, DONE y MIC (FILE no lo usamos, es para reconocer archivos wav).
    // En función del estado ejecutan distintas cosas.
    // yo fui agregando funciones. Las comento
    private void setUiState(int state) {
        switch (state) {
            case STATE_START:
                //Cuando arranca pone preparando sistema.
                resultView.setText(R.string.preparing);
                // Propiedad para que scrolee
                resultView.setMovementMethod(new ScrollingMovementMethod());
                // Se deshabilita el boton reconocer, porque todavía no está andando el VOSK
                findViewById(R.id.recognize_mic).setEnabled(false);
                // Se guarda el estado "START" por si el usuario rota el celular
                statusUi=1;
                break;
            case STATE_READY:
                //Cuando está listo pone "presione en la oreja.."
                if(statusUi!=4){
                    resultView.setText(R.string.ready);
                    //Se habilita el botón
                    findViewById(R.id.recognize_mic).setEnabled(true);
                    // Se guarda el estado "READY" por si el usuario rota el celular
                    statusUi=2;}
                break;

            case STATE_DONE:
                // Cuando el usuario termina la captura
                recMic = findViewById(R.id.recognize_mic);
                //Se habilitan los botones de compartir y se vuelve a mostrar la oreja en el de reconocer
                recMic.setEnabled(true);
                recMic.setImageResource(R.drawable.oreja_negra);
                shareText = findViewById(R.id.compartir);
                shareText.setVisibility(View.VISIBLE);
                statusUi=3;
                resetApp = false;
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(150);
                break;
            case STATE_FILE:

                //No usado
                resultView.setText(getString(R.string.starting));
                findViewById(R.id.recognize_mic).setEnabled(false);

                break;
            case STATE_MIC:
                //Acá se entra cuando se está reconociendo.
                // Le agregue el iff con resetApp por si el usuario rotó el celular durante una transcripción
                if(!resetApp){
                resultView.setText(getString(R.string.say_something));}
                // Habilitamos el botón de reconocer y le ponemos el ícono pausa.
                recMic = findViewById(R.id.recognize_mic);
                recMic.setEnabled(true);
                recMic.setImageResource(R.drawable.ic_pause_24px);
                Vibrator vibrator2 = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator2.vibrate(150);
                statusUi=4;
                break;
        }
    }

    private void setErrorState(String message) {
        resultView.setText(message);


        findViewById(R.id.recognize_mic).setEnabled(false);
    }

    public void recognizeFile() {
        setUiState(STATE_FILE);
        new RecognizeTask(this, resultView).execute();
    }

    public void recognizeMicrophone() {
        if (recognizer != null) {
            setUiState(STATE_DONE);
            recognizer.cancel();
            recognizer = null;
        } else {
            setUiState(STATE_MIC);
            try {
                recognizer = new SpeechRecognizer(model);
                recognizer.addListener(this);
                recognizer.startListening();
            } catch (IOException e) {
                setErrorState(e.getMessage());
            }
        }
    }

    // Esto es por si el usuario rota el teléfono. Los métodos onSaveInstanceState y
    // onRestoreInstanceState permiten tomar ciertas variables antes de que se resetee , y volver a usarlas
    // Lo que hice fue guardar el texto transcripto hasta ese motomento para volver a mostrarlo luego

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        TextoCompleto = resultView.getText().toString() +'\n';
        outState.putString("texto", TextoCompleto.toUpperCase());
        outState.putInt("status", statusUi);
        if(statusUi==4){
        outState.putBoolean("reset",true);
        }


    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        resetApp = savedInstanceState.getBoolean("reset");

        if(resetApp){
            recMic.setImageResource(R.drawable.ic_pause_24px);
            TextoCompleto = savedInstanceState.getString("texto");
            resultView.setText(TextoCompleto);
            statusUi = savedInstanceState.getInt("status");
            recMic.setEnabled(true);
        }

    }

    // Método llamado cuando se presiona el botón atrás.
    // Se muestra un "toast" (mensaje flotante)
    // Si se presiona atrás de nuevo se cierra la app
    @Override
    public void onBackPressed() {
        if (this.lastBackPressTime < System.currentTimeMillis() - 8000) {
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
