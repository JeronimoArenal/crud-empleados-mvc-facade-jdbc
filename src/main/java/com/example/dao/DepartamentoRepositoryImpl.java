package com.example.dao;

import com.example.config.DBConexion;
import com.example.model.Departamento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DepartamentoRepositoryImpl implements DepartamentoRepository {

    private final DBConexion dbConexion = new DBConexion("root", "Kose9suf");
    private static final Logger LOGGER = Logger.getLogger(DepartamentoRepositoryImpl.class.getName());

    //................................. findAll ..............................................
    @Override
    public List<Departamento> findAll() {
        List<Departamento> lista = new ArrayList<>();
        // Seleccionamos Departamentos
        String sql = "SELECT id, nombre FROM empresa_crud_empleados.departamentos";

        /**
         * Try-with-resources cierra automáticamente los recursos utilizados, debe implementar la interface
         *  java.lang.AutoCloseable. Connection, PreparedStatement y ResultSet implementan esta interface
         */
        try (Connection conn = dbConexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);     //Prepara la consulta
             ResultSet rs = ps.executeQuery()) {                    //Ejecuta la consulta

            while (rs.next()) {                                //Empieza a recorrer las filas
                lista.add(new Departamento(                    //Transforma cada fila de la DB en un objeto Java y lo guarda en la lista.
                        rs.getInt("id"),
                        rs.getString("nombre")
                ));
            }
            LOGGER.info("Departamentos cargados correctamente desde su propio repositorio.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar los departamentos en DepartamentoRepositoryImpl", e);
        }
        return lista;
    }
}
