package com.teste.eduardo.unbayesteste.model;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

/**
 * Created by Eduardo on 29/08/2017.
 * Classe responsavel por identificar atividades nos sensores.
 * Implementa o servico que roda em background.
 */

public class ActivityRecognizedService extends IntentService {

    public static String LOCAL_BROADCAST = "LOCAL_BROADCAST";
    public static String CAMINHANDO = "CAMINHANDO";
    public static String TOCANDO_MUSICA = "TOCANDO_MUSICA";
    public static String ASSISTINDO_MIDIA = "ASSISTINDO_MIDIA";
    public static String TELA_LIGADA = "TELA_LIGADA";

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean estaCaminhando = false;
        boolean estaTocandoMusica = false;
        boolean telaLigada = false;

        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult resultado = ActivityRecognitionResult.extractResult(intent);
            estaCaminhando = verificaAtividades( resultado.getProbableActivities() );
        }
        AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if(manager.isMusicActive()) {
            estaTocandoMusica = true;
        }

        // Usada em api menor que API20?
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager.isScreenOn()){
            telaLigada = true;
        }

        informeAtividade(estaCaminhando, estaTocandoMusica, telaLigada);
    }

    private boolean verificaAtividades(List<DetectedActivity> provaveisAtividades) {
        boolean estaCaminhando = false;
        for( DetectedActivity atividade : provaveisAtividades ) {
            switch( atividade.getType() ) {
                case DetectedActivity.ON_FOOT: {
                    if( atividade.getConfidence() >= 75 ) {
                        estaCaminhando = true;
                    }
                    break;
                }
                case DetectedActivity.RUNNING: {
                    if( atividade.getConfidence() >= 75 ) {
                        estaCaminhando = true;
                    }
                    break;
                }
                case DetectedActivity.WALKING: {
                    if( atividade.getConfidence() >= 75 ) {
                        estaCaminhando = true;
                    }
                    break;
                }
            }
        }
        return estaCaminhando;
    }

    private void informeAtividade(boolean caminhando, boolean tocandoMusica, boolean telaLigada) {
        Intent intent = new Intent(LOCAL_BROADCAST);
        intent.putExtra(CAMINHANDO, caminhando);
        intent.putExtra(TOCANDO_MUSICA, tocandoMusica);
        intent.putExtra(TELA_LIGADA, telaLigada);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
