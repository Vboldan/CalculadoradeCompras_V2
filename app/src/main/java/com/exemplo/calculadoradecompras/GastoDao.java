package com.exemplo.calculadoradecompras;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GastoDao {

    @Insert
    void inserir(GastoEntity gasto);

    @Query("SELECT * FROM gastos ORDER BY id DESC")
    List<GastoEntity> obterTodosGastos();

    @Query("DELETE FROM gastos")
    void limparTudo();
}
