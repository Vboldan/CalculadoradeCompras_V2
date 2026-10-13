package com.exemplo.calculadoradecompras;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
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

        // Botão EXCLUIR (Remove os itens selecionados por toque)
        btnExcConta.setOnClickListener(v -> excluirItensSelecionados());

        // Botão SALVAR (Salva, limpa tudo e retorna o cursor para o campo inicial)
        btnSalvarContas.setOnClickListener(v -> salvarE1Limpar());

        // Botão ENVIAR (Gera e compartilha o PDF / Relatório)
        btnEnviarContas.setOnClickListener(v -> gerarECompartilharPDF());

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

    private void salvarE1Limpar() {
        if (listaContas.isEmpty()) {
            Toast.makeText(getContext(), "Nenhuma conta adicionada para salvar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aqui você pode armazenar no banco/preferências se necessário,
        // limpando os dados e posicionando o cursor no primeiro campo
        listaContas.clear();
        entRendaContas.setText("");
        entNomeConta.setText("");
        entValorConta.setText("");
        atualizarListaEStatus();

        entRendaContas.requestFocus();
        Toast.makeText(getContext(), "Contas salvas e campos limpos com sucesso!", Toast.LENGTH_LONG).show();
    }

    private void atualizarListaEStatus() {
        boxListaContas.removeAllViews();

        for (int i = 0; i < listaContas.size(); i++) {
            final ContaItem item = listaContas.get(i);

            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            row.setPadding(12, 14, 12, 14);
            row.setGravity(Gravity.CENTER_VERTICAL);

            if (item.selecionado) {
                row.setBackgroundColor(0xFF505c71);
            } else {
                row.setBackgroundColor(0x00000000);
            }

            TextView tvNome = new TextView(getContext());
            tvNome.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
            ));
            tvNome.setText(item.nome + ":");
            tvNome.setTextColor(0xFFFFFFFF);
            tvNome.setTextSize(16);

            TextView tvValor = new TextView(getContext());
            tvValor.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            tvValor.setText(String.format(Locale.getDefault(), "R$ %.2f", item.valor));
            tvValor.setTextColor(0xFF00FFFF); // Cor ciano solicitada
            tvValor.setTextSize(16);

            row.addView(tvNome);
            row.addView(tvValor);

            row.setOnClickListener(v -> {
                item.selecionado = !item.selecionado;
                atualizarListaEStatus();
            });

            boxListaContas.addView(row);
        }

        atualizarResumo();
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

    private void gerarECompartilharPDF() {
        if (listaContas.isEmpty()) {
            Toast.makeText(getContext(), "Nenhuma conta para gerar PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            StringBuilder textoPdf = new StringBuilder();
            textoPdf.append("RELATORIO DE CONTAS FIXAS\n");
            textoPdf.append(lblDataContas.getText().toString()).append("\n\n");

            double renda = 0.0;
            try {
                renda = Double.parseDouble(entRendaContas.getText().toString().trim().replace(",", "."));
            } catch (Exception ignored) {}

            textoPdf.append(String.format(Locale.getDefault(), "Renda: R$ %.2f\n\n", renda));
            textoPdf.append("Lista de Contas:\n");

            double total = 0.0;
            for (ContaItem item : listaContas) {
                textoPdf.append(String.format(Locale.getDefault(), "- %s: R$ %.2f\n", item.nome, item.valor));
                total += item.valor;
            }

            double restante = renda - total;
            textoPdf.append(String.format(Locale.getDefault(), "\nTotal Contas: R$ %.2f", total));
            textoPdf.append(String.format(Locale.getDefault(), "\nRestante: R$ %.2f", restante));

            // Criação do ficheiro PDF temporário
            File pdfFile = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ContasFixas.pdf");
            
            // Simples geração de texto encapsulado (ou gravação direta)
            FileOutputStream fos = new FileOutputStream(pdfFile);
            fos.write(textoPdf.toString().getBytes());
            fos.close();

            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", pdfFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Partilhar Relatório PDF"));

        } catch (Exception e) {
            Toast.makeText(getContext(), "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}