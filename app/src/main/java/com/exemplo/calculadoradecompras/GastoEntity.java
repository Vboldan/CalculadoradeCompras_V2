package com.exemplo.calculadoradecompras;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "gastos")
public class GastoEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String data;
    private String categoria;
    private double valor;
    private String descricao;

    public GastoEntity(String data, String categoria, double valor, String descricao) {
        this.data = data;
        this.categoria = categoria;
        this.valor = valor;
        this.descricao = descricao;
    }

    // Getters públicos para acessar os dados com segurança
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }
}