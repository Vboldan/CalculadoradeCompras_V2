package com.exemplo.calculadoradecompras;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CsvHelper {

    private static final String FILE_NAME = "historico_compras.csv";

    public static void adicionarRegistros(Context context, String categoria, List<RegistroCsvItem> itens) {
        if (itens == null || itens.isEmpty()) return;

        File documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (documentsDir != null && !documentsDir.exists()) {
            documentsDir.mkdirs();
        }

        File csvFile = new File(documentsDir, FILE_NAME);
        boolean arquivoNovo = !csvFile.exists();

        long proximoId = calcularProximoId(csvFile);

        // 'true' ativa o modo append (anexa no final do ficheiro sem apagar)
        try (FileWriter writer = new FileWriter(csvFile, true)) {
            if (arquivoNovo) {
                writer.append("id,data_hora,categoria,descricao,valor\n");
            }

            for (RegistroCsvItem item : itens) {
                writer.append(String.valueOf(proximoId)).append(",")
                      .append("\"").append(item.dataHora).append("\",")
                      .append("\"").append(categoria).append("\",")
                      .append("\"").append(item.descricao.replace("\"", "\"\"")).append("\",")
                      .append(String.format(Locale.US, "%.2f", item.valor)).append("\n");
                proximoId++;
            }
            writer.flush();
        } catch (IOException e) {
            Log.e("CsvHelper", "Erro ao gravar no arquivo CSV: " + e.getMessage());
        }
    }

    private static long calcularProximoId(File csvFile) {
        if (!csvFile.exists()) return 1;

        long totalLinhas = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            while (reader.readLine() != null) {
                totalLinhas++;
            }
        } catch (IOException ignored) {}

        // Se só tem a linha de cabeçalho (1 linha), o primeiro ID é 1.
        return Math.max(1, totalLinhas);
    }

    public static class RegistroCsvItem {
        String dataHora;
        String descricao;
        double valor;

        public RegistroCsvItem(String dataHora, String descricao, double valor) {
            this.dataHora = dataHora;
            this.descricao = descricao;
            this.valor = valor;
        }
    }
}