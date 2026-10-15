package com.exemplo.calculadoradecompras;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Locale;

public class HistoricoFragment extends Fragment {

    private LinearLayout boxListaHistorico;
    private Button btnLimparHistorico;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        boxListaHistorico = view.findViewById(R.id.boxListaHistorico);
        btnLimparHistorico = view.findViewById(R.id.btnLimparHistorico);

        btnLimparHistorico.setOnClickListener(v -> confirmarLimpeza());
        carregarHistorico();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarHistorico();
    }

    private void carregarHistorico() {
        boxListaHistorico.removeAllViews();

        new Thread(() -> {
            List<GastoEntity> gastos = AppDatabase.getDatabase(requireContext()).gastoDao().obterTodosGastos();

            requireActivity().runOnUiThread(() -> {
                if (gastos.isEmpty()) {
                    TextView tvVazio = new TextView(getContext());
                    tvVazio.setText("Nenhum registro encontrado no histórico.");
                    tvVazio.setTextColor(Color.GRAY);
                    tvVazio.setTextSize(14);
                    tvVazio.setGravity(Gravity.CENTER);
                    tvVazio.setPadding(16, 32, 16, 16);
                    boxListaHistorico.addView(tvVazio);
                    return;
                }

                for (GastoEntity g : gastos) {
                    LinearLayout row = new LinearLayout(getContext());
                    row.setOrientation(LinearLayout.VERTICAL);
                    row.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    row.setPadding(16, 14, 16, 14);
                    row.setBackgroundColor(0xFF252525);

                    // Linha superior: Categoria (Sem colchetes) e Data
                    LinearLayout topRow = new LinearLayout(getContext());
                    topRow.setOrientation(LinearLayout.HORIZONTAL);
                    topRow.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));

                    TextView tvCategoria = new TextView(getContext());
                    tvCategoria.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
                    tvCategoria.setText(g.getCategoria()); // Sem colchetes
                    tvCategoria.setTextColor(0xFF00FFFF);
                    tvCategoria.setTextSize(13);
                    tvCategoria.setTypeface(null, android.graphics.Typeface.BOLD);

                    TextView tvData = new TextView(getContext());
                    tvData.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    tvData.setText(g.getData());
                    tvData.setTextColor(Color.parseColor("#CCCCCC"));
                    tvData.setTextSize(11);

                    topRow.addView(tvCategoria);
                    topRow.addView(tvData);

                    // Nome do Item (Ex: Internet, Luz, Arroz)
                    TextView tvDescricao = new TextView(getContext());
                    tvDescricao.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    tvDescricao.setText(g.getDescricao());
                    tvDescricao.setTextColor(Color.WHITE);
                    tvDescricao.setTextSize(15);
                    tvDescricao.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvDescricao.setPadding(0, 4, 0, 4);

                    // Valor Individual
                    TextView tvValor = new TextView(getContext());
                    tvValor.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    tvValor.setText(String.format(Locale.getDefault(), "R$ %.2f", g.getValor()));
                    tvValor.setTextColor(0xFF00FF00);
                    tvValor.setTextSize(14);

                    row.addView(topRow);
                    row.addView(tvDescricao);
                    row.addView(tvValor);

                    View separador = new View(getContext());
                    LinearLayout.LayoutParams sepParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 6);
                    separador.setLayoutParams(sepParams);

                    boxListaHistorico.addView(row);
                    boxListaHistorico.addView(separador);
                }
            });
        }).start();
    }

    private void confirmarLimpeza() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Limpar Histórico")
                .setMessage("Deseja realmente apagar todos os registros salvos no banco de dados?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    new Thread(() -> {
                        AppDatabase.getDatabase(requireContext()).gastoDao().limparTudo();
                        requireActivity().runOnUiThread(() -> {
                            carregarHistorico();
                            Toast.makeText(getContext(), "Histórico limpo com sucesso!", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("Não", null)
                .show();
    }
}