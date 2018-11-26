package com.teste.eduardo.unbayesteste.model;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import unbbayes.io.NetIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * Created by Eduardo on 12/12/2017.
 * Classe responsavel por carregar a RB e realizar a inferencia.
 * Faz uso da biblioteca do UnbBayes.
 */

public class RedeBayesiana {

    private Context context;
    private String arquivoRB;
    private ProbabilisticNetwork rede;
    private IInferenceAlgorithm alg;

    public RedeBayesiana(Context context) {
        this.context = context;
        AssetManager am = context.getAssets();
        InputStream inputStream = null;

        //Nome do arquivo a ser carregado
        arquivoRB = "ConscienciaSituacional.net";
        try {
            inputStream = am.open(arquivoRB);
        } catch (IOException e) {
            e.printStackTrace();
        }
        criarArquivoInputStream(inputStream, arquivoRB);
        try {
            inicializa();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void inicializa() throws Exception {
        rede = (ProbabilisticNetwork) new NetIO().load(new File(context.getFilesDir(), arquivoRB));
        // prepara o algoritmo para compilar a rede
        alg = new JunctionTreeAlgorithm();
        alg.setNetwork(rede);
        alg.run();
    }

    public String retornaRB() {
        String redeImpressa = "------ Rede Bayesiana ------"+ System.getProperty("line.separator");
        for (Node node : rede.getNodes()) {
            redeImpressa = redeImpressa + node.getName() + System.getProperty("line.separator");
            for (int i = 0; i < node.getStatesSize(); i++) {
                redeImpressa = redeImpressa + node.getStateAt(i) + " : "
                        + ((ProbabilisticNode)node).getMarginalAt(i) + System.getProperty("line.separator");
            }
        }
        return redeImpressa;
    }

    public void atualizaRB(String evidencia) {
        // insere evidencia (finding) na rede considerando o no
        ProbabilisticNode noEvidencia = (ProbabilisticNode) rede.getNode(evidencia);
        noEvidencia.addFinding(0); // o estado eh agora 100%
        // propaga a evidencia
        alg.propagate();
    }

    public Float retornaConsciencia() {
        Node node = rede.getNode("Consciente");
        Float valorConsciencia = ((ProbabilisticNode)node).getMarginalAt(0);
        return valorConsciencia;
    }

    public void resetaRB(){
        alg.reset();
    }

    //Carrega o arquivo localizado na pasta assets
    private File criarArquivoInputStream(InputStream inputStream, String nomeArquivo) {
        try {
            File arquivo = new File(context.getFilesDir(), nomeArquivo);
            OutputStream outputStream = new FileOutputStream(arquivo);
            byte buffer[] = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return arquivo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
