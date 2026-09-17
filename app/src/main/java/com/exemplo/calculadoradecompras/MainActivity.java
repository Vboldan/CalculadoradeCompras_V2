package com.exemplo.calculadoradecompras;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText entLimite, entProduto, entPreco, entQtd;
    private TextView lblStatus, lblData;
    private LinearLayout boxLista;
    private Button btnEnviar;

    private double totalGasto = 0.0;
    private List<Produto> produtos = new ArrayList<>();
    private List<View> itensSelecionados = new ArrayList<>();
    private File ultimoArquivoPdf = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#23272d"));


        entLimite = findViewById(R.id.entLimite);
        entProduto = findViewById(R.id.entProduto);
        entPreco = findViewById(R.id.entPreco);
        entQtd = findViewById(R.id.entQtd);
        lblStatus = findViewById(R.id.lblStatus);
        lblData = findViewById(R.id.lblData);
        boxLista = findViewById(R.id.boxLista);
        btnEnviar = findViewById(R.id.btnEnviar);

        String dataStr = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault()).format(new Date());
        lblData.setText("Data: " + dataStr);

        findViewById(R.id.btnAdd).setOnClickListener(v -> adicionarProduto());
        findViewById(R.id.btnExc).setOnClickListener(v -> removerProduto());
        findViewById(R.id.btnSalvar).setOnClickListener(v -> salvarPdf());
        btnEnviar.setOnClickListener(v -> enviarPdf());

        TextView tvFooter = findViewById(R.id.tvFooter);
        tvFooter.setOnClickListener(v -> {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Vboldan/CalculadoradeCompras_V2"));
        startActivity(intent);
        });

        entLimite.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) atualizarStatus();
        });
    }

    private double obterLimite() {
        try {
            String texto = entLimite.getText().toString().replace(",", ".");
            return texto.isEmpty() ? 0.0 : Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void atualizarStatus() {
        double limite = obterLimite();
        double restante = limite - totalGasto;
        lblStatus.setText(String.format(Locale.getDefault(), "Total: R$%.2f | Restante: R$%.2f", totalGasto, restante));
    }

    private void adicionarProduto() {
        double limite = obterLimite();
        if (limite == 0.0) {
            mostrarAlerta("Erro", "Digite o valor disponível!");
            return;
        }

        String nome = entProduto.getText().toString().trim();
        String precoStr = entPreco.getText().toString().replace(",", ".");
        String qtdStr = entQtd.getText().toString();

        if (nome.isEmpty() || precoStr.isEmpty()) {
            mostrarAlerta("Erro", "Preencha o produto e preço corretamente!");
            return;
        }

        try {
            double preco = Double.parseDouble(precoStr);
            int qtd = qtdStr.isEmpty() ? 1 : Integer.parseInt(qtdStr);
            double subtotal = preco * qtd;

            if (totalGasto + subtotal > limite) {
                mostrarAlerta("Erro", "Ultrapassa o limite disponível!");
                return;
            }

            totalGasto += subtotal;
            Produto p = new Produto(nome, preco, qtd);
            produtos.add(p);

            LinearLayout rowItem = new LinearLayout(this);
            rowItem.setOrientation(LinearLayout.HORIZONTAL);
            rowItem.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            rowItem.setPadding(24, 16, 24, 16);
            rowItem.setTag(p);

            TextView txtNome = new TextView(this);
            txtNome.setText(nome + ":");
            txtNome.setTextColor(Color.WHITE);
            txtNome.setTextSize(14);
            LinearLayout.LayoutParams paramsNome = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            txtNome.setLayoutParams(paramsNome);
            txtNome.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

            TextView txtValor = new TextView(this);
            txtValor.setText(String.format(Locale.getDefault(), "R$%.2f x %d = R$%.2f", preco, qtd, subtotal));
            txtValor.setTextColor(Color.WHITE);
            txtValor.setTextSize(14);
            LinearLayout.LayoutParams paramsValor = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            txtValor.setLayoutParams(paramsValor);
            txtValor.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

            rowItem.addView(txtNome);
            rowItem.addView(txtValor);

            rowItem.setOnClickListener(v -> {
                if (itensSelecionados.contains(rowItem)) {
                    itensSelecionados.remove(rowItem);
                    rowItem.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    itensSelecionados.add(rowItem);
                    rowItem.setBackgroundColor(Color.DKGRAY);
                }
            });

            boxLista.addView(rowItem);

            entProduto.setText("");
            entPreco.setText("");
            entQtd.setText("1");
            entProduto.requestFocus();

            atualizarStatus();

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Valores numéricos inválidos!");
        }
    }

    private void removerProduto() {
        if (itensSelecionados.isEmpty()) {
            mostrarAlerta("Aviso", "Selecione um ou mais itens para excluir!");
            return;
        }

        for (View v : itensSelecionados) {
            Produto p = (Produto) v.getTag();
            produtos.remove(p);
            totalGasto -= p.getSubtotal();
            boxLista.removeView(v);
        }
        itensSelecionados.clear();
        atualizarStatus();
    }

    private void salvarPdf() {
        if (produtos.isEmpty()) {
            mostrarAlerta("Aviso", "Nenhum produto na lista!");
            return;
        }

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Título Principal
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText("NOTA DE COMPRA", 220, 50, paint);

        // Data / Hora
        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        String dataStr = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault()).format(new Date());
        canvas.drawText("Data/Hora: " + dataStr, 220, 70, paint);

        int y = 110;

        // Fundo colorido para o cabeçalho da tabela
        Paint bgHeaderPaint = new Paint();
        bgHeaderPaint.setColor(Color.parseColor("#3B88f4"));
        bgHeaderPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(40, y - 15, 520, y + 8, bgHeaderPaint);

        // Texto do cabeçalho (Branco e Negrito)
        paint.setColor(Color.WHITE);
        paint.setFakeBoldText(true);
        canvas.drawText("Produto", 50, y, paint);
        canvas.drawText("Preço", 250, y, paint);
        canvas.drawText("Qtd", 350, y, paint);
        canvas.drawText("Subtotal", 420, y, paint);

        y += 25;

        // Restaura a cor do texto para preto para listar os produtos
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(false);

        Produto maisCaro = produtos.get(0);

        for (Produto p : produtos) {
            if (p.getPreco() > maisCaro.getPreco()) maisCaro = p;
            canvas.drawText(p.getNome(), 50, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "R$ %.2f", p.getPreco()), 250, y, paint);
            canvas.drawText(String.valueOf(p.getQuantidade()), 350, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "R$ %.2f", p.getSubtotal()), 420, y, paint);
            y += 20;
        }

        y += 30;
        paint.setFakeBoldText(true);
        canvas.drawText(String.format(Locale.getDefault(), "VALOR DISPONÍVEL: R$ %.2f", obterLimite()), 50, y, paint);
        y += 20;
        paint.setFakeBoldText(false);
        canvas.drawText(String.format(Locale.getDefault(), "TOTAL GASTO: R$ %.2f", totalGasto), 50, y, paint);
        y += 20;
        canvas.drawText(String.format(Locale.getDefault(), "SALDO RESTANTE: R$ %.2f", obterLimite() - totalGasto), 50, y, paint);
        y += 20;
        canvas.drawText(String.format(Locale.getDefault(), "PRODUTO MAIS CARO: %s (R$ %.2f)", maisCaro.getNome(), maisCaro.getPreco()), 50, y, paint);

        document.finishPage(page);

        File dir = getExternalFilesDir(null);
        ultimoArquivoPdf = new File(dir, "lista_de_compras.pdf");

        try {
            document.writeTo(new FileOutputStream(ultimoArquivoPdf));
            btnEnviar.setEnabled(true);
            mostrarAlerta("Sucesso", "PDF salvo com sucesso em:\n" + ultimoArquivoPdf.getAbsolutePath());
            
            // Reseta a interface e foca em Disponível após salvar
            limparTudo();
        } catch (IOException e) {
            mostrarAlerta("Erro", "Erro ao salvar PDF: " + e.getMessage());
        } finally {
            document.close();
        }
    }

    private void limparTudo() {
        produtos.clear();
        itensSelecionados.clear();
        boxLista.removeAllViews();
        totalGasto = 0.0;
https://github.com/Vboldan/CalculadoradeCompras_V2
        entLimite.setText("");
        entProduto.setText("");
        entPreco.setText("");
        entQtd.setText("1");

        atualizarStatus();
        entLimite.requestFocus();
    }

    private void enviarPdf() {
        if (ultimoArquivoPdf == null || !ultimoArquivoPdf.exists()) {
            mostrarAlerta("Erro", "Arquivo PDF não encontrado. Salve primeiro.");
            return;
        }

        try {
            Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", ultimoArquivoPdf);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Compartilhar Lista de Compras"));
        } catch (Exception e) {
            mostrarAlerta("Erro", "Erro ao compartilhar: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setPositiveButton("OK", null)
                .show();
    }
}
