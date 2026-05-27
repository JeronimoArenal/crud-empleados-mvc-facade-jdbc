package com.example.service;

import com.example.dao.EmpleadoRepository;
import com.example.dao.EmpleadoRepositoryImpl;
import com.example.model.Empleado;

import java.util.List;

public class EmpleadoServiceImpl implements EmpleadoService {

    // El servicio consume la interfaz del repositorio
    private final EmpleadoRepository empleadoRepository;

    // Al estilo Spring Boot, asignamos la implementación real en el constructor
    public EmpleadoServiceImpl() {
        this.empleadoRepository = new EmpleadoRepositoryImpl();
    }

    //................................. IsConnectionOK ..............................................
    @Override
    public boolean isConnectionOK() {
        return empleadoRepository.checkConnection();
    }

    //................................. listarEmpleados ..............................................
    @Override
    public List<Empleado> listarEmpleados() {     // Pedimos los datos al repositorio y devolverlos
        return empleadoRepository.findAll();
    }
}