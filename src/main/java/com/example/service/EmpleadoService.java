package com.example.service;

import com.example.model.Empleado;

import java.util.List;

public interface EmpleadoService {

    boolean isConnectionOK();
    List<Empleado> listarEmpleados();
}
