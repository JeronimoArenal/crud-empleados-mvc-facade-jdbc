package com.example.dao;

import com.example.config.DBConexion;
import com.example.controller.MainController;
import com.example.model.Empleado;
import com.example.model.Genero;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmpleadoRepositoryImpl implements EmpleadoRepository {
    // La base de datos se maneja exclusivamente desde AQUÍ
    private final DBConexion dbConexion = new DBConexion("root", "Kose9suf");
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());


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
//        String sql =  "SELECT * FROM empresa-crud-empleados.empleados";
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
            LOGGER.log(Level.SEVERE, "Error al ejecutar la consulta findAll en empleados", e.getMessage());
        }
        return lista;
    }

}
