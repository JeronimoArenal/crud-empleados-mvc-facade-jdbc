package com.example.model;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record Empleado(
        int id,
        String nombre,
        String primerApellido,
        String segundoApellido,
        Genero genero,
        LocalDate fechaAlta,
        double salario,
        Departamento departamento,
//        int idDpto,
        List<String> telefonos,
        List<String> correos
) {
    // El compilador genera automáticamente:
    // - Un constructor con todos los campos.
    // - Métodos de acceso: id(), nombre(), salario(), etc. (sin el prefijo "get").
    // - equals(), hashCode() y toString().
}
