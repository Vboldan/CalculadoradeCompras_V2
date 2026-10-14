package com.exemplo.calculadoradecompras;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
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
    private File pdfGeradoFile = null;

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

        // Preencher Data no topo
        SimpleDateFormat sdf = new SimpleDateFormat("'Data:' dd/MM/yyyy - HH:mm", Locale.getDefault());
        lblDataContas.setText(sdf.format(new Date()));

        // Atualizar resumo ao alterar renda
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

        btnAddConta.setOnClickListener(v -> adicionarConta());
        btnExcConta.setOnClickListener(v -> excluirItensSelecionados());

        // SALVAR: Gera o PDF padronizado, registra no banco Room, limpa a tela e foca no primeiro campo
        btnSalvarContas.setOnClickListener(v -> salvarEGerarPDF());

        // ENVIAR: Partilha o PDF criado
        btnEnviarContas.setOnClickListener(v -> compartilharPDF());

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

    private void salvarEGerarPDF() {
        if (listaContas.isEmpty()) {
            Toast.makeText(getContext(), "Nenhuma conta adicionada para salvar", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Calcula o total das contas
        double totalContas = 0.0;
        for (ContaItem item : listaContas) {
            totalContas += item.valor;
        }

        // 2. Gera o arquivo PDF padronizado com marca d'água
        pdfGeradoFile = criarPDFPadronizado();

        if (pdfGeradoFile != null) {
            Toast.makeText(getContext(), "PDF de Contas Fixas gerado com sucesso!", Toast.LENGTH_SHORT).show();
            btnEnviarContas.setEnabled(true);

            // 3. SALVAMENTO AUTOMÁTICO NO BANCO DE DADOS ROOM (SEGURO EM BACKGROUND)
            String dataSalvar = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            final double finalTotal = totalContas;
            
            GastoEntity gastoConta = new GastoEntity(dataSalvar, "Contas Fixas", finalTotal, "Relatório de Contas Fixas");

            new Thread(() -> {
                AppDatabase.getDatabase(requireContext()).gastoDao().inserir(gastoConta);
            }).start();
        }

        // 4. Limpa os dados e reseta a interface
        listaContas.clear();
        entRendaContas.setText("");
        entNomeConta.setText("");
        entValorConta.setText("");
        atualizarListaEStatus();

        // 5. Foco de volta no primeiro campo
        entRendaContas.requestFocus();
    }

    private File criarPDFPadronizado() {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // Formato A4
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int y = 40; // Posição inicial vertical para o conteúdo

        // --- MARCA D'ÁGUA ---
        try {
            Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icone);
            if (iconBitmap != null) {
                int larguraDesejada = 350;
                int alturaDesejada = 350;
                Bitmap scaledIcon = Bitmap.createScaledBitmap(iconBitmap, larguraDesejada, alturaDesejada, true);

                Paint watermarkPaint = new Paint();
                watermarkPaint.setAlpha(35);

                int posX = (pageInfo.getPageWidth() - scaledIcon.getWidth()) / 2;
                int posY = (pageInfo.getPageHeight() - scaledIcon.getHeight()) / 2;

                canvas.drawBitmap(scaledIcon, posX, posY, watermarkPaint);
            }
        } catch (Exception ignored) {}

        // Título Principal centralizado
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        canvas.drawText("RELATÓRIO DE CONTAS FIXAS", 150, y, paint);

        y += 20;
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        String dataHoraStr = lblDataContas.getText().toString().replace("Data: ", "Data/Hora: ");
        canvas.drawText(dataHoraStr, 170, y, paint);

        y += 35;

        // Cabeçalho da Tabela
        paint.setColor(Color.parseColor("#3B88f4")); // Cor do cabeçalho (azul)
        paint.setStyle(Paint.Style.FILL); 
        canvas.drawRect(40, y - 15, 520, y + 8, paint); // Retângulo de fundo do cabeçalho

        paint.setColor(Color.WHITE);
        paint.setTextSize(13);
        paint.setFakeBoldText(true);
        canvas.drawText("Conta", 55, y + 4, paint);
        canvas.drawText("Valor", 450, y + 4, paint);

        y += 35;

        // Itens da Tabela
        paint.setFakeBoldText(false);
        paint.setTextSize(12);
        paint.setColor(Color.BLACK);

        double totalContas = 0.0;
        for (ContaItem item : listaContas) {
            canvas.drawText(item.nome, 55, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "R$ %.2f", item.valor), 450, y, paint);
            totalContas += item.valor;
            y += 25;
        }

        y += 15;
        paint.setStrokeWidth(1);
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(40, y, 555, y, paint);

        y += 30;

        // Bloco de Resumo (Renda Disponível, Total e Saldo Restante)
        double renda = 0.0;
        try {
            renda = Double.parseDouble(entRendaContas.getText().toString().trim().replace(",", "."));
        } catch (Exception ignored) {}

        double restante = renda - totalContas;

        paint.setFakeBoldText(true);
        paint.setTextSize(13);
        paint.setColor(Color.BLACK);
        canvas.drawText(String.format(Locale.getDefault(), "VALOR DISPONÍVEL: R$ %.2f", renda), 40, y, paint);
        
        y += 20;
        canvas.drawText(String.format(Locale.getDefault(), "TOTAL CONTAS: R$ %.2f", totalContas), 40, y, paint);
        
        y += 20;
        canvas.drawText(String.format(Locale.getDefault(), "SALDO RESTANTE: R$ %.2f", restante), 40, y, paint);

        document.finishPage(page);

        // Salva o PDF na pasta de documentos da app
        File file = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ContasFixas.pdf");
        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            return file;
        } catch (Exception e) {
            document.close();
            Toast.makeText(getContext(), "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void compartilharPDF() {
        if (pdfGeradoFile == null || !pdfGeradoFile.exists()) {
            Toast.makeText(getContext(), "Clique em Salvar para gerar o PDF antes de enviar", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", pdfGeradoFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Partilhar Relatório PDF"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erro ao compartilhar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
            tvValor.setTextColor(0xFF00FFFF); // Cor Ciano solicitada
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
}