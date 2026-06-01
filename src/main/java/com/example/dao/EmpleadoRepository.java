package com.example.dao;


import com.example.model.Empleado;

import java.util.List;

public interface EmpleadoRepository {
        boolean checkConnection(); // Método para validar la BD
        List<Empleado> findAll();
        void save(Empleado empleado);
        Empleado findById(int id);
        void update(Empleado empleado);
        void delete(int id);
}
