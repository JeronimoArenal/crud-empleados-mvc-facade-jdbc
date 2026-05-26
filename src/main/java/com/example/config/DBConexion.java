package com.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class DBConexion {

    private final String username;
    private final String password;
    private static final Logger LOGGER = Logger.getLogger("DBConexion");

    //.................... Constructor ............................
    public DBConexion(String username, String password) {      // Recibe las credenciales dinámicamente
        this.username = username;
        this.password = password;
    }

    //................................ getConnection .............................................
    // Importante: Lanza SQLException para que el Service sepa si falló
    public Connection getConnection() throws SQLException {
        String urlConnection = "jdbc:mysql://192.168.122.43:3306/empresa-crud-empleados?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        Properties info = new Properties();
        info.put("user", this.username); // MySQL JDBC usa "user", no "username"
        info.put("password", this.password);

        try {
            // Registramos el driver explícitamente por seguridad
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(urlConnection, info);
            LOGGER.info("Conectado exitosamente a la Base de Datos");
            return conn;
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Error: No se encontró el driver JDBC de MySQL");
            throw new SQLException("Driver no encontrado", e);
        } catch (SQLException e) {
            LOGGER.severe("Error de base de datos al intentar conectar");
            throw e; // Propagamos el error hacia arriba
        }
    }
}

