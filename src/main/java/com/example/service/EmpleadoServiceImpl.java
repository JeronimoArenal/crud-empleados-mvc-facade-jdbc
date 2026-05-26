package com.example.service;

import com.example.config.DBConexion;
import java.sql.Connection;
import java.sql.SQLException;

public class EmpleadoServiceImpl implements IEmpleadoService {

    private final DBConexion dbConexion;     // Declaramos la variable de configuración como atributo de la clase


    //............................. Constructor .......................................
    public EmpleadoServiceImpl(DBConexion dbConexion) {     // Recibe la configuración desde el MainController
        this.dbConexion = dbConexion;
    }

    //................................. IsConnectionOK ..............................................
    @Override
    public boolean isConnectionOK() {

        // Abrimos la conexión DENTRO del try-with-resources para asegurar su cierre automático
        try (Connection conn = dbConexion.getConnection()) {
            // Si llega aquí sin lanzar excepción y no está cerrada, la conexión funciona
            return conn != null && !conn.isClosed();

        } catch (SQLException e) {
            // Si el método getConnection() falló, entra aquí y devuelve false de forma segura
            System.err.println("La comprobación de conexión ha fallado: " + e.getMessage());
            return false;
        }
    }
}
