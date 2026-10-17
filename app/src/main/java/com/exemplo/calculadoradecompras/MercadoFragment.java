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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

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
    private TextView lblStatus, lblData;
    private LinearLayout boxLista;
    private Button btnEnviar;

    private double totalGasto = 0.0;
    private List<Produto> produtos = new ArrayList<>();
    private List<View> itensSelecionados = new ArrayList<>();
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
        boxLista = view.findViewById(R.id.boxLista);
        btnEnviar = view.findViewById(R.id.btnEnviar);

        String dataStr = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault()).format(new Date());
        lblData.setText("Data: " + dataStr);

        view.findViewById(R.id.btnAdd).setOnClickListener(v -> adicionarProduto());
        view.findViewById(R.id.btnExc).setOnClickListener(v -> removerProduto());
        view.findViewById(R.id.btnSalvar).setOnClickListener(v -> salvarPdf());
        btnEnviar.setOnClickListener(v -> enviarPdf());

        TextView tvFooter = view.findViewById(R.id.tvFooter);
        tvFooter.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Vboldan/CalculadoradeCompras_V2"));
            startActivity(intent);
        });

        entLimite.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) atualizarStatus();
        });

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        // Garante que o cursor e o teclado fiquem visíveis no campo do Limite assim que a aba Mercado for aberta
        if (entLimite != null) {
            entLimite.postDelayed(() -> {
                if (entLimite != null && isAdded()) {
                    entLimite.requestFocus();
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(entLimite, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }, 200);
        }
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
            Toast.makeText(getContext(), "Digite o valor disponível!", Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = entProduto.getText().toString().trim();
        String precoStr = entPreco.getText().toString().replace(",", ".");
        String qtdStr = entQtd.getText().toString();

        if (nome.isEmpty() || precoStr.isEmpty()) {
            Toast.makeText(getContext(), "Preencha o produto e preço corretamente!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double preco = Double.parseDouble(precoStr);
            int qtd = qtdStr.isEmpty() ? 1 : Integer.parseInt(qtdStr);
            double subtotal = preco * qtd;

            if (totalGasto + subtotal > limite) {
                Toast.makeText(getContext(), "Ultrapassa o limite disponível!", Toast.LENGTH_SHORT).show();
                return;
            }

            totalGasto += subtotal;
            Produto p = new Produto(nome, preco, qtd);
            produtos.add(p);

            LinearLayout rowItem = new LinearLayout(requireContext());
            rowItem.setOrientation(LinearLayout.HORIZONTAL);
            rowItem.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            rowItem.setPadding(24, 16, 24, 16);
            rowItem.setTag(p);

            TextView txtNome = new TextView(requireContext());
            txtNome.setText(nome + ":");
            txtNome.setTextColor(Color.WHITE);
            txtNome.setTextSize(14);
            LinearLayout.LayoutParams paramsNome = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            txtNome.setLayoutParams(paramsNome);
            txtNome.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

            TextView txtValor = new TextView(requireContext());
            txtValor.setText(String.format(Locale.getDefault(), "R$%.2f x %d = R$%.2f", preco, qtd, subtotal));
            txtValor.setTextColor(0xFF00FFFF);
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
            Toast.makeText(getContext(), "Produto adicionado!", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Valores numéricos inválidos!", Toast.LENGTH_SHORT).show();
        }
    }

    private void removerProduto() {
        if (itensSelecionados.isEmpty()) {
            Toast.makeText(getContext(), "Selecione um ou mais itens para excluir!", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getContext(), "Itens removidos!", Toast.LENGTH_SHORT).show();
    }

    private void limparTudo() {
        produtos.clear();
        itensSelecionados.clear();
        boxLista.removeAllViews();
        totalGasto = 0.0;

        entLimite.setText("");
        entProduto.setText("");
        entPreco.setText("");
        entQtd.setText("1");

        atualizarStatus();
        entLimite.requestFocus();
    }

    private void enviarPdf() {
        if (ultimoArquivoPdf == null || !ultimoArquivoPdf.exists()) {
            Toast.makeText(getContext(), "Arquivo PDF não encontrado. Salve primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", ultimoArquivoPdf);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Compartilhar Lista de Compras"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erro ao compartilhar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void salvarPdf() {
        if (produtos.isEmpty()) {
            Toast.makeText(getContext(), "Nenhum produto na lista!", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

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

            String dataSalvar = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            
            new Thread(() -> {
                GastoDao dao = AppDatabase.getDatabase(requireContext()).gastoDao();
                for (Produto p : produtos) {
                    String descItem = p.getNome() + " (Qtd: " + p.getQuantidade() + ")";
                    GastoEntity g = new GastoEntity(dataSalvar, "Mercado", p.getSubtotal(), descItem);
                    dao.inserir(g);
                }
            }).start();

            Toast.makeText(getContext(), "PDF salvo e registrado no Histórico com sucesso!", Toast.LENGTH_LONG).show();

            limparTudo();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Erro ao salvar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
        }
    }
}