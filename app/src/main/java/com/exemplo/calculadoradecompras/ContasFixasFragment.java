package com.vboldan.calculadoradecompras;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ContasFixasFragment extends Fragment {

    private TextView lblDataContas, lblStatusContas;
    private EditText entRendaContas, entNomeConta, entValorConta;
    private Button btnAddConta, btnExcConta, btnSalvarContas, btnEnviarContas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contas_fixas, container, false);

        // Referência dos componentes do layout
        lblDataContas = view.findViewById(R.id.lblDataContas);
        lblStatusContas = view.findViewById(R.id.lblStatusContas);
        entRendaContas = view.findViewById(R.id.entRendaContas);
        entNomeConta = view.findViewById(R.id.entNomeConta);
        entValorConta = view.findViewById(R.id.entValorConta);

        btnAddConta = view.findViewById(R.id.btnAddConta);
        btnExcConta = view.findViewById(R.id.btnExcConta);
        btnSalvarContas = view.findViewById(R.id.btnSalvarContas);
        btnEnviarContas = view.findViewById(R.id.btnEnviarContas);

        // Insere a Data Atual no formato exato da aba Mercado (Ex: Data: 12/10/2026 - 23:34)
        SimpleDateFormat sdf = new SimpleDateFormat("'Data:' dd/MM/yyyy - HH:mm", Locale.getDefault());
        lblDataContas.setText(sdf.format(new Date()));

        // Ativa os botões com eventos de clique
        btnAddConta.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Adicionar conta clicado!", Toast.LENGTH_SHORT).show();
        });

        btnExcConta.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Excluir conta clicado!", Toast.LENGTH_SHORT).show();
        });

        btnSalvarContas.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Salvar contas clicado!", Toast.LENGTH_SHORT).show();
        });

        btnEnviarContas.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Enviar relatorio clicado!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}