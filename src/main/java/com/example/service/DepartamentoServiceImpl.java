package com.example.service;

import com.example.dao.DepartamentoRepository;
import com.example.dao.DepartamentoRepositoryImpl;
import com.example.model.Departamento;

import java.util.List;

public class DepartamentoServiceImpl implements DepartamentoService{

    private final DepartamentoRepository deptoRepository = new DepartamentoRepositoryImpl();

    //................................. listarDepartamentos ..............................................
    @Override
    public List<Departamento> listarDepartamentos() {
        return deptoRepository.findAll();
    }
}
