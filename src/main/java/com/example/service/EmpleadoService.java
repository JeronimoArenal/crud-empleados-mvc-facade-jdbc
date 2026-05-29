package com.example.service;

import com.example.model.Departamento;
import com.example.model.Empleado;

import java.util.List;

public interface EmpleadoService {

    boolean isConnectionOK();
    List<Empleado> listarEmpleados();
    void guardar(Empleado emp);
    Empleado buscarPorId(int id);
    void modificar(Empleado emp);

}
