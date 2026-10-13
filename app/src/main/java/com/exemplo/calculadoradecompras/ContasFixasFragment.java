package com.exemplo.calculadoradecompras;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

    // Estrutura interna para guardar as contas
    private static class ContaItem {
        String nome;
        double valor;

        ContaItem(String nome, double valor) {
            this.nome = nome;
            this.valor = valor;
        }
    }

    private final List<ContaItem> listaContas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contas_fixas, container, false);

        // Mapear elementos do layout
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

        // Preencher a Data Atual
        SimpleDateFormat sdf = new SimpleDateFormat("'Data:' dd/MM/yyyy - HH:mm", Locale.getDefault());
        lblDataContas.setText(sdf.format(new Date()));

        // Atualizar cálculos se mudar a Renda
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

        // Botão EXCLUIR
        btnExcConta.setOnClickListener(v -> removerUltimaConta());

        // Botão SALVAR
        btnSalvarContas.setOnClickListener(v -> salvarContas());

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

            // Limpa campos de entrada da conta
            entNomeConta.setText("");
            entValorConta.setText("");
            entNomeConta.requestFocus();

            atualizarListaEStatus();
            Toast.makeText(getContext(), "Conta adicionada!", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Valor inválido!", Toast.LENGTH_SHORT).show();
        }
    }

    private void removerUltimaConta() {
        if (!listaContas.isEmpty()) {
            listaContas.remove(listaContas.size() - 1);
            atualizarListaEStatus();
            Toast.makeText(getContext(), "Última conta removida", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Nenhuma conta para excluir", Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarContas() {
        if (listaContas.isEmpty()) {
            Toast.makeText(getContext(), "Nenhuma conta adicionada para salvar", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(), "Contas salvas com sucesso!", Toast.LENGTH_LONG).show();
    }

    private void atualizarListaEStatus() {
        boxListaContas.removeAllViews();

        for (ContaItem item : listaContas) {
            TextView tv = new TextView(getContext());
            tv.setText(String.format(Locale.getDefault(), "• %s - R$ %.2f", item.nome, item.valor));
            tv.setTextColor(0xFFFFFFFF); // Branco
            tv.setTextSize(15);
            tv.setPadding(8, 8, 8, 8);
            boxListaContas.addView(tv);
        }

        atualizarResumo();
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
                "Total Contas: R$%.2f | Restante: R$%.2f", totalContas, restante));
    }
}