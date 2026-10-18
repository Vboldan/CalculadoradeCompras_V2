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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MercadoFragment extends Fragment {

    private EditText entLimite, entProduto, entPreco, entQtd;
    private TextView lblStatus, lblData, lblItemMaisCaro;
    private Button btnSalvar, btnEnviar;
    private LinearLayout boxLista;

    private static class Produto {
        private final String nome;
        private final double preco;
        private final int quantidade;
        private boolean selecionado;

        public Produto(String nome, double preco, int quantidade) {
            this.nome = nome;
            this.preco = preco;
            this.quantidade = quantidade;
            this.selecionado = false;
        }

        public String getNome() { return nome; }
        public double getPreco() { return preco; }
        public int getQuantidade() { return quantidade; }
        public double getSubtotal() { return preco * quantidade; }
        public boolean isSelecionado() { return selecionado; }
        public void setSelecionado(boolean selecionado) { this.selecionado = selecionado; }
    }

    private final List<Produto> produtos = new ArrayList<>();
    private double totalGasto = 0.0;
    private File ultimoArquivoPdf = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mercado, container, false);

        entLimite = view.findViewById(R.id.entLimite);
        entProduto = view.findViewById(R.id.entProduto);
        entPreco = view.findViewById(R.id.entPreco);
        entQtd = view.findViewById(R.id.entQtd);

        lblStatus = view.findViewById(R.id.lblStatus);
        lblData = view.findViewById(R.id.lblData);
        lblItemMaisCaro = view.findViewById(R.id.lblItemMaisCaro);

        Button btnAdicionar = view.findViewById(R.id.btnAdicionar);
        Button btnRemover = view.findViewById(R.id.btnRemover);
        btnSalvar = view.findViewById(R.id.btnSalvar);
        btnEnviar = view.findViewById(R.id.btnEnviar);

        boxLista = view.findViewById(R.id.boxLista);

        SimpleDateFormat sdf = new SimpleDateFormat("'Data:' dd/MM/yyyy - HH:mm", Locale.getDefault());
        lblData.setText(sdf.format(new Date()));

        btnAdicionar.setOnClickListener(v -> adicionarProduto());
        btnRemover.setOnClickListener(v -> removerSelecionados());
        btnSalvar.setOnClickListener(v -> salvarPdf());
        btnEnviar.setOnClickListener(v -> compartilharPdf());

        return view;
    }

    private double obterLimite() {
        String s = entLimite.getText().toString().trim();
        if (s.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void adicionarProduto() {
        String nome = entProduto.getText().toString().trim();
        String precoStr = entPreco.getText().toString().trim();
        String qtdStr = entQtd.getText().toString().trim();

        if (nome.isEmpty() || precoStr.isEmpty()) {
            Toast.makeText(getContext(), "Informe o nome e o preço do produto", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double preco = Double.parseDouble(precoStr.replace(",", "."));
            int qtd = qtdStr.isEmpty() ? 1 : Integer.parseInt(qtdStr);

            produtos.add(new Produto(nome, preco, qtd));

            entProduto.setText("");
            entPreco.setText("");
            entQtd.setText("1");
            entProduto.requestFocus();

            atualizarLista();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Valores inválidos!", Toast.LENGTH_SHORT).show();
        }
    }

    private void removerSelecionados() {
        List<Produto> paraRemover = new ArrayList<>();
        for (Produto p : produtos) {
            if (p.isSelecionado()) paraRemover.add(p);
        }
        produtos.removeAll(paraRemover);
        atualizarLista();
    }

    private void limparTudo() {
        produtos.clear();
        entProduto.setText("");
        entPreco.setText("");
        entQtd.setText("1");
        atualizarLista();
    }

    private void atualizarLista() {
        boxLista.removeAllViews();
        totalGasto = 0.0;
        Produto maisCaro = null;

        for (int i = 0; i < produtos.size(); i++) {
            final Produto p = produtos.get(i);
            totalGasto += p.getSubtotal();

            if (maisCaro == null || p.getPreco() > maisCaro.getPreco()) {
                maisCaro = p;
            }

            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(12, 14, 12, 14);

            if (p.isSelecionado()) {
                itemLayout.setBackgroundColor(0xFF505C71);
            } else {
                itemLayout.setBackgroundColor(0x00000000);
            }

            TextView tvNome = new TextView(getContext());
            tvNome.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            tvNome.setText(String.format("%s (x%d)", p.getNome(), p.getQuantidade()));
            tvNome.setTextColor(0xFFFFFFFF);
            tvNome.setTextSize(16);

            TextView tvValor = new TextView(getContext());
            tvValor.setText(String.format(Locale.getDefault(), "R$ %.2f", p.getSubtotal()));
            tvValor.setTextColor(0xFF00FFFF);
            tvValor.setTextSize(16);

            itemLayout.addView(tvNome);
            itemLayout.addView(tvValor);

            itemLayout.setOnClickListener(v -> {
                p.setSelecionado(!p.isSelecionado());
                atualizarLista();
            });

            boxLista.addView(itemLayout);
        }

        double limite = obterLimite();
        double restante = limite - totalGasto;

        lblStatus.setText(String.format(Locale.getDefault(), "Total: R$ %.2f | Restante: R$ %.2f", totalGasto, restante));

        if (maisCaro != null) {
            lblItemMaisCaro.setText(String.format(Locale.getDefault(), "Mais caro: %s (R$ %.2f)", maisCaro.getNome(), maisCaro.getPreco()));
        } else {
            lblItemMaisCaro.setText("Mais caro: -");
        }
    }

    private void salvarPdf() {
        if (produtos.isEmpty()) {
            Toast.makeText(getContext(), "Nenhum produto na lista para salvar!", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        try {
            Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icone);
            if (iconBitmap != null) {
                Bitmap scaledIcon = Bitmap.createScaledBitmap(iconBitmap, 350, 350, true);
                Paint watermarkPaint = new Paint();
                watermarkPaint.setAlpha(35);
                int posX = (pageInfo.getPageWidth() - scaledIcon.getWidth()) / 2;
                int posY = (pageInfo.getPageHeight() - scaledIcon.getHeight()) / 2;
                canvas.drawBitmap(scaledIcon, posX, posY, watermarkPaint);
            }
        } catch (Exception ignored) {}

        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        canvas.drawText("NOTA DE COMPRA", 220, 50, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        String dataStr = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault()).format(new Date());
        canvas.drawText("Data/Hora: " + dataStr, 220, 70, paint);

        int y = 110;
        paint.setColor(Color.parseColor("#3B88f4"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(40, y - 15, 520, y + 8, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(13);
        paint.setFakeBoldText(true);
        canvas.drawText("Produto", 55, y + 4, paint);
        canvas.drawText("Preço", 250, y + 4, paint);
        canvas.drawText("Qtd", 380, y + 4, paint);
        canvas.drawText("Subtotal", 450, y + 4, paint);

        y += 25;
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

        File dir = requireContext().getExternalFilesDir(null);
        ultimoArquivoPdf = new File(dir, "lista_de_compras.pdf");

        try {
            document.writeTo(new FileOutputStream(ultimoArquivoPdf));
            btnEnviar.setEnabled(true);

            // Anexa os itens no arquivo CSV na ordem correta
            String dataSalvar = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
            List<CsvHelper.RegistroCsvItem> itensCsv = new ArrayList<>();
            for (Produto p : produtos) {
                String descItem = p.getNome() + " (Qtd: " + p.getQuantidade() + ")";
                itensCsv.add(new CsvHelper.RegistroCsvItem(dataSalvar, descItem, p.getSubtotal()));
            }
            CsvHelper.adicionarRegistros(requireContext(), "Mercado", itensCsv);

            Toast.makeText(getContext(), "PDF gerado e dados salvos no CSV!", Toast.LENGTH_LONG).show();
            limparTudo();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Erro ao salvar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
        }
    }

    private void compartilharPdf() {
        if (ultimoArquivoPdf == null || !ultimoArquivoPdf.exists()) {
            Toast.makeText(getContext(), "Salve o PDF antes de enviar", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", ultimoArquivoPdf);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Partilhar Nota de Compra"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erro ao compartilhar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}