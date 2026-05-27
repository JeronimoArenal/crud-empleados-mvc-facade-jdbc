package com.example.dao;

import com.example.config.DBConexion;
import com.example.model.Empleado;
import com.example.model.Genero;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoRepositoryImpl implements EmpleadoRepository {
    // La base de datos se maneja exclusivamente AQUÍ
    private final DBConexion dbConexion = new DBConexion("root", "Kose9suf");

    //................................. checkConnection ..............................................
    @Override
    public boolean checkConnection() {
        try (Connection conn = dbConexion.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return false;
        }
    }

    //................................. findAll ..............................................
    @Override
    public List<Empleado> findAll() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, primerApellido, segundoApellido, fechaAlta, genero, salario FROM empleados";

        try (Connection conn = dbConexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // 1. Extraer datos primitivos y cadenas básicos
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String primerApellido = rs.getString("primerApellido");
                String segundoApellido = rs.getString("segundoApellido");
                double salario = rs.getDouble("salario");

                // 2. Usar explícitamente java.sql.Date para la conversión limpia
                Date sqlDate = rs.getDate("fechaAlta");
                LocalDate fechaAlta = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                // 3. Convertir el texto de la base de datos al Enum Genero
                String generoBD = rs.getString("genero");
                Genero genero = (generoBD != null) ? Genero.valueOf(generoBD.trim().toUpperCase()) : null;

                // 4. Instanciar el record Empleado usando su constructor completo.
                Empleado emp = new Empleado(
                        id,
                        nombre,
                        primerApellido,
                        segundoApellido,
                        genero,
                        fechaAlta,
                        salario,
                        null,       // departamento
                        List.of(),  // telefonos
                        List.of()   // correos
                );

                lista.add(emp);
            }
        } catch (SQLException e) {
            System.err.println("Error en Repository.findAll: " + e.getMessage());
        }
        return lista;
    }

    public List<Empleado> getEmpleados(Connection connection) {
        List<Empleado> empleados = new ArrayList<>();
        String query = "SELECT id, nombre, primerApellido, segundoApellido, fechaAlta, genero, salario FROM empleados";

        // El Statement y el ResultSet deben gestionarse dentro del try para controlar las excepciones de SQL
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                // 1. Extraer los valores controlando posibles nulos
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String primerApellido = rs.getString("primerApellido"); // Corregido: "primerApelllido" tenía una 'l' de más
                String segundoApellido = rs.getString("segundoApellido");

                java.sql.Date sqlDate = rs.getDate("fechaAlta");
                java.time.LocalDate fechaAlta = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                String generoBD = rs.getString("genero");
                Genero genero = null;
                if (generoBD != null) {
                    try {
                        genero = Genero.valueOf(generoBD.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        System.err.println("Género desconocido: " + generoBD);
                    }
                }

                double salario = rs.getDouble("salario"); // Corregido: rs.getDoublle

                // 2. Al ser un Record, NO EXISTE .builder(). Se usa el constructor completo directamente:
                Empleado emp = new Empleado(
                        id,
                        nombre,
                        primerApellido,
                        segundoApellido,
                        genero,
                        fechaAlta,
                        salario,
                        null,       // departamento (lo dejamos null temporalmente)
                        List.of(),  // telefonos
                        List.of()   // correos
                );

                empleados.add(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return empleados; // Devolvemos la lista de records procesada, no el ResultSet abierto
    }
}
