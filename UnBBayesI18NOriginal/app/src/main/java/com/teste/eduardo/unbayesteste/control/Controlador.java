package com.teste.eduardo.unbayesteste.control;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.teste.eduardo.unbayesteste.model.ActivityRecognizedService;
import com.teste.eduardo.unbayesteste.model.RedeBayesiana;
import com.teste.eduardo.unbayesteste.view.PrincipalActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Eduardo on 12/12/2017.
 * Classe que faz a intermediacao entre a interface grafica e o dominio do problema.
 * Faz alguns tratamentos das informacoes que chegam.
 */

public class Controlador extends BroadcastReceiver implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Context context;
    private int TEMPO = 3000; // segundos
    private PrincipalActivity interfaceGrafica;
    private GoogleApiClient apiClient;
    private RedeBayesiana redeBayesiana;

    private boolean estaFalandoCelular;

    public Controlador( ) {
    }

    public void inicialize(PrincipalActivity principalActivity) {
        this.context = principalActivity.getBaseContext();
        interfaceGrafica = principalActivity;
        redeBayesiana = new RedeBayesiana(context);
        estaFalandoCelular = false;

        //Inicializa a API para identificar caminhada
        apiClient = new GoogleApiClient.Builder(context)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        apiClient.connect();

        Calendar calendario = Calendar.getInstance();
        SimpleDateFormat mascaraData = new SimpleDateFormat("dd/MM/yyyy");
        String strData = "Data de hoje: " + mascaraData.format(calendario.getTime());
        String infoExtra = "Este texto foi adicionado para teste I18N. Trata-se de um aplicativo desenvolvido para o meu TCC. " +
                System.getProperty("line.separator") + "Número teste: 1.234.567,89 Região do formato: pt_BR" +
                System.getProperty("line.separator") + strData + System.getProperty("line.separator");

        interfaceGrafica.mostreInfoExtra(infoExtra);

        String infoRB = null;
        try {
            infoRB = redeBayesiana.retornaRB();
        } catch (Exception e) {
            e.printStackTrace();
        }
        interfaceGrafica.mostreInfoRB(infoRB);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Cria um intent, ou token, que permite o servico tratar a identificacao de caminhada em um tempo futuro
        Intent intent = new Intent(context, ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService( context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(apiClient, TEMPO, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    public void onResume(PrincipalActivity principalActivity) {
        //Registra um receiver localmente
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(this, new IntentFilter(ActivityRecognizedService.LOCAL_BROADCAST));
    }

    public void onPause(PrincipalActivity principalActivity) {
    }

    //Recebe informacoes dos servicos que rodam em background
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean estaCaminhando = true;
        boolean telaLigada = false;
        boolean estaTocandoMusica = false;

        //Trata as informacoes relacionadas aos sensores
        if(intent.getAction().equals(ActivityRecognizedService.LOCAL_BROADCAST)){
            estaCaminhando = intent.getBooleanExtra(ActivityRecognizedService.CAMINHANDO, false);
            telaLigada = intent.getBooleanExtra(ActivityRecognizedService.TELA_LIGADA, false);
            estaTocandoMusica = intent.getBooleanExtra(ActivityRecognizedService.TOCANDO_MUSICA, false);
        }
        //Trata as informacoes relacionadas a ligacoes
        if(intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                estaFalandoCelular = true;
            }
        }
        if(estaCaminhando) {
            interfaceGrafica.mostreInfoEvidencia("Caminhando");
            interfaceGrafica.mostreInfoEvidencia("Tela Ligada");
            interfaceGrafica.notificarPopup("Atenção! Caminhar e utilizar o smartphone não é seguro!");
            if(estaTocandoMusica) {
                interfaceGrafica.mostreInfoEvidencia("Tocando Musica ou Som");
                redeBayesiana.atualizaRB("DistracaoApp");
            }
            if(estaFalandoCelular){
                interfaceGrafica.mostreInfoEvidencia("Falando Celular");
                redeBayesiana.atualizaRB("DistracaoApp");
            }
            interfaceGrafica.mostreInfoInferencia(redeBayesiana.retornaRB());
        }

        //Depois de tratadas as informacoes, mede o nivel de consciencia e notifica
        Float consciencia = redeBayesiana.retornaConsciencia();
        if(consciencia < 0.79){
            interfaceGrafica.notificar("Nível de consciência abaixo de 0.79!");
            interfaceGrafica.notificarPopup("Atenção! Você pode estar distraído!");
            estaFalandoCelular = false;
            redeBayesiana.resetaRB();
        }
    }
}
