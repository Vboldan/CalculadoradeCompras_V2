package com.exemplo.calculadoradecompras;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "gastos")
public class GastoEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String data;
    private String categoria; // Ex: "Mercado", "Água", "Luz", "Gás", "Internet"
    private double valor;
    private String descricao; // Ex: "Supermercado do mês" ou "Conta de luz Copel"

    // Construtor
    public GastoEntity(String data, String categoria, double valor, String descricao) {
        this.data = data;
        this.categoria = categoria;
        this.valor = valor;
        this.descricao = descricao;
    }

    // Métodos Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
