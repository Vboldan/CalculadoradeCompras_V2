package com.exemplo.calculadoradecompras;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HistoricoFragment extends Fragment {

    private LinearLayout containerHistorico;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        containerHistorico = view.findViewById(R.id.containerHistorico);
        Button btnLimparHistorico = view.findViewById(R.id.btnLimparHistorico);
        Button btnExportarCsv = view.findViewById(R.id.btnExportarCsv);

        btnLimparHistorico.setOnClickListener(v -> limparHistorico());
        
        if (btnExportarCsv != null) {
            btnExportarCsv.setOnClickListener(v -> exportarParaCsv());
        }

        carregarHistorico();
        return view;
    }

    private void carregarHistorico() {
        new Thread(() -> {
            GastoDao dao = AppDatabase.getDatabase(requireContext()).gastoDao();
            List<GastoEntity> lista = dao.obterTodosGastos();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> montarListaVisual(lista));
            }
        }).start();
    }

    private void montarListaVisual(List<GastoEntity> lista) {
        containerHistorico.removeAllViews();

        if (lista.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("Nenhum registro encontrado.");
            emptyText.setTextColor(0xFF888888);
            emptyText.setPadding(32, 32, 32, 32);
            containerHistorico.addView(emptyText);
            return;
        }

        for (GastoEntity item : lista) {
            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(24, 16, 24, 16);

            TextView txtCategoriaData = new TextView(getContext());
            txtCategoriaData.setText(String.format("%s - %s", item.getCategoria(), item.getData()));
            txtCategoriaData.setTextColor(0xFF00FFFF);
            txtCategoriaData.setTextSize(12);

            TextView txtDescricao = new TextView(getContext());
            txtDescricao.setText(item.getDescricao());
            txtDescricao.setTextColor(0xFFFFFFFF);
            txtDescricao.setTextSize(16);

            TextView txtValor = new TextView(getContext());
            txtValor.setText(String.format(Locale.getDefault(), "R$ %.2f", item.getValor()));
            txtValor.setTextColor(0xFF00FF00);
            txtValor.setTextSize(14);

            itemLayout.addView(txtCategoriaData);
            itemLayout.addView(txtDescricao);
            itemLayout.addView(txtValor);

            containerHistorico.addView(itemLayout);
        }
    }

    private void exportarParaCsv() {
        new Thread(() -> {
            GastoDao dao = AppDatabase.getDatabase(requireContext()).gastoDao();
            List<GastoEntity> lista = dao.obterTodosGastos();

            if (lista.isEmpty()) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Não há dados para exportar!", Toast.LENGTH_SHORT).show()
                    );
                }
                return;
            }

            File csvFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "historico_compras.csv");

            try (FileWriter writer = new FileWriter(csvFile)) {
                // Escreve o cabeçalho do arquivo CSV
                writer.append("id,data_hora,categoria,descricao,valor\n");

                // Escreve cada registro usando os Getters
                for (GastoEntity g : lista) {
                    writer.append(String.valueOf(g.getId())).append(",")
                          .append("\"").append(g.getData()).append("\",")
                          .append("\"").append(g.getCategoria()).append("\",")
                          .append("\"").append(g.getDescricao().replace("\"", "\"\"")).append("\",")
                          .append(String.format(Locale.US, "%.2f", g.getValor())).append("\n");
                }

                writer.flush();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> compartilharCsv(csvFile));
                }

            } catch (IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Erro ao gerar CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }

    private void compartilharCsv(File csvFile) {
        try {
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", csvFile);
            
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Exportar Histórico CSV"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erro ao compartilhar CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void limparHistorico() {
        new Thread(() -> {
            GastoDao dao = AppDatabase.getDatabase(requireContext()).gastoDao();
            dao.limparTudo();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    carregarHistorico();
                    Toast.makeText(getContext(), "Histórico limpo com sucesso!", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}