package com.exemplo.calculadoradecompras;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.exemplo.calculadoradecompras.R;

public class ContasFixasFragment extends Fragment {

    private TextView lblDataContas, lblStatusContas;
    private EditText entRendaContas, entNomeConta, entValorConta;
    private Button btnAddConta, btnExcConta, btnSalvarContas, btnEnviarContas;
    private LinearLayout boxListaContas;

    private static class ContaItem {
        String nome;
        double valor;
        boolean selecionado;

        ContaItem(String nome, double valor) {
            this.nome = nome;
            this.valor = valor;
            this.selecionado = false;
        }
    }

    private final List<ContaItem> listaContas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contas_fixas, container, false);

        lblDataContas = view.findViewById(R.id.lblDataContas);
        lblStatusContas = view.findViewById(R.id.lblStatusContas);
        entRendaContas = view.findViewById(R.id.entRendaContas);
        entNomeConta = view.findViewById(R.id.entNomeConta);
        entValorConta = view.findViewById(R.id.entValorConta);
        boxListaContas = view.findViewById(R.id.boxListaContas);

        btnAddConta = view.findViewById(R.id.btnAddConta);
        btnExcConta = view.findViewById(R.id.btnExcConta);
        btnSalvarContas = view.findViewById(R.id.btnSalvarContas);
        btnEnviarContas = view.findViewById(R.id.btnEnviarContas);

        // Preencher a Data Atual no topo
        SimpleDateFormat sdf = new SimpleDateFormat("'Data:' dd/MM/yyyy - HH:mm", Locale.getDefault());
        lblDataContas.setText(sdf.format(new Date()));

        // Atualizar cálculos ao alterar a Renda
        entRendaContas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                atualizarResumo();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Botão ADICIONAR
        btnAddConta.setOnClickListener(v -> adicionarConta());

        // Botão EXCLUIR (Remove os itens selecionados por toque, igual ao Mercado)
        btnExcConta.setOnClickListener(v -> excluirItensSelecionados());

        // Botão SALVAR
        btnSalvarContas.setOnClickListener(v -> {
            if (listaContas.isEmpty()) {
                Toast.makeText(getContext(), "Adicione contas antes de salvar", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getContext(), "Contas salvas com sucesso!", Toast.LENGTH_SHORT).show();
        });

        // Botão ENVIAR (Compartilha o relatório)
        btnEnviarContas.setOnClickListener(v -> enviarRelatorio());

        atualizarListaEStatus();
        return view;
    }

    private void adicionarConta() {
        String nome = entNomeConta.getText().toString().trim();
        String valorStr = entValorConta.getText().toString().trim();

        if (nome.isEmpty()) {
            Toast.makeText(getContext(), "Informe o nome da conta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (valorStr.isEmpty()) {
            Toast.makeText(getContext(), "Informe o valor da conta", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double valor = Double.parseDouble(valorStr.replace(",", "."));
            listaContas.add(new ContaItem(nome, valor));

            entNomeConta.setText("");
            entValorConta.setText("");
            entNomeConta.requestFocus();

            atualizarListaEStatus();
            Toast.makeText(getContext(), "Conta adicionada!", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Valor inválido!", Toast.LENGTH_SHORT).show();
        }
    }

    private void excluirItensSelecionados() {
        List<ContaItem> paraRemover = new ArrayList<>();
        for (ContaItem item : listaContas) {
            if (item.selecionado) {
                paraRemover.add(item);
            }
        }

        if (paraRemover.isEmpty()) {
            Toast.makeText(getContext(), "Toque em um item da lista para selecioná-lo antes de excluir", Toast.LENGTH_SHORT).show();
            return;
        }

        listaContas.removeAll(paraRemover);
        atualizarListaEStatus();
        Toast.makeText(getContext(), "Itens selecionados excluídos", Toast.LENGTH_SHORT).show();
    }

    private void atualizarListaEStatus() {
        boxListaContas.removeAllViews();

        for (int i = 0; i < listaContas.size(); i++) {
            final ContaItem item = listaContas.get(i);

            // Layout da linha horizontal idêntico ao Mercado
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            row.setPadding(12, 14, 12, 14);
            row.setGravity(Gravity.CENTER_VERTICAL);

            // Altera a cor de fundo se o item estiver selecionado por toque
            if (item.selecionado) {
                row.setBackgroundColor(0xFF505c71); // Tom cinza/azulado de destaque igual ao Mercado
            } else {
                row.setBackgroundColor(0x00000000); // Transparente
            }

            // Nome da Conta (à esquerda)
            TextView tvNome = new TextView(getContext());
            tvNome.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
            ));
            tvNome.setText(item.nome + ":");
            tvNome.setTextColor(0xFFFFFFFF);
            tvNome.setTextSize(16);

            // Valor da Conta (à direita)
            TextView tvValor = new TextView(getContext());
            tvValor.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            tvValor.setText(String.format(Locale.getDefault(), "R$ %.2f", item.valor));
            tvValor.setTextColor(0xFF00FFFF);
            tvValor.setTextSize(16);

            row.addView(tvNome);
            row.addView(tvValor);

            // Ação de toque para selecionar/desselecionar o item (padrão da aba Mercado)
            row.setOnClickListener(v -> {
                item.selecionado = !item.selecionado;
                atualizarListaEStatus();
            });

            boxListaContas.addView(row);
        }

        atualizarResumo();

        // Ativa o botão Enviar apenas se houver contas na lista
        btnEnviarContas.setEnabled(!listaContas.isEmpty());
    }

    private void atualizarResumo() {
        double renda = 0.0;
        String rendaStr = entRendaContas.getText().toString().trim();
        if (!rendaStr.isEmpty()) {
            try {
                renda = Double.parseDouble(rendaStr.replace(",", "."));
            } catch (NumberFormatException ignored) {}
        }

        double totalContas = 0.0;
        for (ContaItem item : listaContas) {
            totalContas += item.valor;
        }

        double restante = renda - totalContas;

        lblStatusContas.setText(String.format(Locale.getDefault(),
                "Total Contas: R$ %.2f | Restante: R$ %.2f", totalContas, restante));
    }

    private void enviarRelatorio() {
        if (listaContas.isEmpty()) {
            Toast.makeText(getContext(), "Nenhuma conta para enviar", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 *RELATÓRIO DE CONTAS FIXAS*\n");
        sb.append(lblDataContas.getText().toString()).append("\n\n");

        double renda = 0.0;
        try {
            renda = Double.parseDouble(entRendaContas.getText().toString().trim().replace(",", "."));
        } catch (Exception ignored) {}

        sb.append(String.format(Locale.getDefault(), "Renda: R$ %.2f\n\n", renda));
        sb.append("Contas:\n");

        double total = 0.0;
        for (ContaItem item : listaContas) {
            sb.append(String.format(Locale.getDefault(), "• %s: R$ %.2f\n", item.nome, item.valor));
            total += item.valor;
        }

        double restante = renda - total;
        sb.append(String.format(Locale.getDefault(), "\nTotal Contas: R$ %.2f", total));
        sb.append(String.format(Locale.getDefault(), "\nRestante: R$ %.2f", restante));

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Contas Fixas");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(intent, "Enviar Relatório via"));
    }
}