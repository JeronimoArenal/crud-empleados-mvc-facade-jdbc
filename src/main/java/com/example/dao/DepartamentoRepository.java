package com.example.dao;

import com.example.model.Departamento;

import java.util.List;

public interface DepartamentoRepository {
    List<Departamento> findAll();
}
