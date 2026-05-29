package com.example.service;

import com.example.dao.DepartamentoRepository;
import com.example.dao.DepartamentoRepositoryImpl;
import com.example.dao.EmpleadoRepository;
import com.example.dao.EmpleadoRepositoryImpl;
import com.example.model.Empleado;

import java.util.List;

public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final DepartamentoRepository departamentoRepository = new DepartamentoRepositoryImpl();

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
    public List<Empleado> listarEmpleados() {
        return empleadoRepository.findAll();
    }

    //................................. buscarPorId ..............................................
    @Override
    public Empleado buscarPorId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del empleado a buscar no es válido.");
        }
        return empleadoRepository.findById(id);
    }

    //................................. guardar ..............................................
    @Override
    public void guardar(Empleado emp) {
        // Ejecutamos el motor de validaciones antes de tocar la base de datos
        ejecutarValidacionesDeNegocio(emp);
        empleadoRepository.save(emp);
    }

    //................................. modificar ..............................................
    @Override
    public void modificar(Empleado emp) {
        if (emp.id() <= 0) {
            throw new IllegalArgumentException("No se puede modificar un empleado sin un ID válido.");
        }
        // Ejecutamos el mismo motor de validaciones
        ejecutarValidacionesDeNegocio(emp);
        empleadoRepository.update(emp);
    }

    //................................. Validaciones Privadas Centralizadas ..............................................
    /**
     * Valida las reglas de negocio del objeto Empleado (Equivale a tu @Valid de Spring Boot)
     */
    private void ejecutarValidacionesDeNegocio(Empleado emp) {
        if (emp.nombre() == null || emp.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del empleado es un campo obligatorio.");
        }

        if (emp.primerApellido() == null || emp.primerApellido().isBlank()) {
            throw new IllegalArgumentException("El primer apellido del empleado es un campo obligatorio.");
        }

        if (emp.salario() < 0) {
            throw new IllegalArgumentException("El salario del empleado no puede ser un valor negativo.");
        }

        if (emp.departamento() == null || emp.departamento().getId() <= 0) {
            throw new IllegalArgumentException("Debe asociar un departamento válido al empleado.");
        }

        // Validación del patrón/formato de email si el campo contiene información
        if (!emp.correos().isEmpty() && emp.correos().get(0) != null && !emp.correos().get(0).isBlank()) {
            String email = emp.correos().get(0);
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("La dirección de correo electrónico introducida no tiene un formato válido.");
            }
        }
    }
}
