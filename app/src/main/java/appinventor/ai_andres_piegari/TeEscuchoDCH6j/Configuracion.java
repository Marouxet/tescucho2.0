package appinventor.ai_andres_piegari.TeEscuchoDCH6j;

//Librerias importadas (muchas se fueron agregando al ir copiando cosas de Google y Youtube)
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

//Clase Configuracion es la app, AppCompactActivity es un tipo de App que tiene Android

public class Configuracion extends AppCompatActivity {

    //Definiciones: Variables y todos los objetos existentes en la parte gráfica

    FloatingActionButton boton;



    @Override
    //Adentró de este método, "onCreate" se pone lo que se produce durante la creación de la actividad. Es decir cuando se crea la pantalla.

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Elegir tema (Colores)
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        boolean modoOscuro = sharedPreferences.getBoolean("ModoOscuro", false);


        if (modoOscuro){

        this.setTheme(R.style.TemaOscuro);}
        else{
            this.setTheme(R.style.TemaClaro);
        }

        setContentView(R.layout.settings_activity);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();



    }

    public void aplicar_cambios(View v) {
        //Si se aplican los cambios, cierro las otras ventanas (Clásico o Continuo) y vuelvo a abrir la app. (abro Inicio)

        if (Clasico.clasicoActiva){
            Clasico.actividad.finish();

        }
        else if(KaldiActivity.continuoActiva){

            KaldiActivity.actividad.finish();
        }


        finish();
        Intent intent2 = new Intent(v.getContext(), Inicio.class);
        startActivityForResult(intent2, 0);
    }



    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }





}