package com.teste.eduardo.unbayesteste.view;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.teste.eduardo.unbayesteste.R;
import com.teste.eduardo.unbayesteste.control.Controlador;

/**
 * Classe que implementa a interface grafica.
 * Mostra informacoes sobre a RB na interface grafica do aplicativo.
 * Tambem notifica o usuario sobre o nivel de consciencia.
 * Tambem exibe mensagens de avisos na tela (Toast)
 */

public class PrincipalActivity extends AppCompatActivity {

        private Controlador controlador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        TextView texto = findViewById(R.id.inferencia);
        texto.setTextColor(Color.DKGRAY);
        String esperandoEvidencia = getResources().getString(R.string.esperandoEvidencia);
        texto.setText(esperandoEvidencia);

        //Solicita permissao para gerenciar ligacoess em tempo de execucao
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_PHONE_STATE},1);
        }

        controlador = new Controlador();

        //Registra um filtro procurando por ligacao
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PHONE_STATE");
        registerReceiver(controlador,intentFilter);

        controlador.inicialize(this);
    }

    public void mostreInfoExtra(String infoExtra) {
        TextView texto = findViewById(R.id.infoextra);
        texto.setTextColor(Color.DKGRAY);
        texto.setText(infoExtra);
    }

    public void mostreInfoRB(String infoRB) {
        TextView texto = findViewById(R.id.rb);
        texto.setTextColor(Color.BLACK);
        texto.setText(infoRB);
    }

    public void mostreInfoEvidencia(String infoRB) {
        TextView texto = findViewById(R.id.evidencia);
        texto.setTextColor(Color.RED);
        texto.setText(infoRB);
    }

    public void mostreInfoInferencia(String infoRB) {
        TextView texto = findViewById(R.id.inferencia);
        texto.setTextColor(Color.BLUE);
        texto.setText(infoRB);
    }

    //Notifica com icone, som e vibracao
    public void notificar(String infoRB) {
        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(this);
        notificacao.setContentText(infoRB);
        notificacao.setSmallIcon( R.mipmap.ic_launcher );
        notificacao.setContentTitle( getString( R.string.app_name ) );
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificacao.setSound(soundUri);
        long[] v = {500,1000};
        notificacao.setVibrate(v);
        NotificationManagerCompat.from(this).notify(0, notificacao.build());
    }

    public void notificarPopup(String infoRB) {
        Toast.makeText(this, infoRB, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        controlador.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
