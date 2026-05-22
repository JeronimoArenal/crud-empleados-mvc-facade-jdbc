package com.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Creamos conexion con patron "Singleton"
public class ConexionDB {
    private static final String URL = "jdbc:mysql://192.168.122.43:3306/empresa-crud-empleados?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Kose9suf";

    //2º. Atributo private static. Almacena en la memoria del servidor la única instancia que existirá de la conexión.
    private static Connection conexion = null;

    //1º Constructor privado. Evita que se puedan crear objetos con new ConexionDB() desde fuera de la clase.
    private ConexionDB() {}

    //3º metodo de acceso controlado. Si la variable conexion está vacía o se cerró, fabrica una nueva;
    // si ya tiene una conexión abierta, no crea otra, devuelve exactamente la misma que ya existía.
    public static Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Error: No se encontró el driver JDBC.", e);
            }
        }
        return conexion;
    }
    /**
     * Lo que NO se debe hacer:
     * conexion.close() al terminar de listar los empleados, destruirás la conexión para toda la aplicación. La próxima petición que intente usar el Singleton fallará o tendrá que reconectar constantemente.
     * Lo que SÍ se debe hacer:
     * En las clases DAO solo se debe cerrar los objetos intermedios, que son el PreparedStatement y el ResultSet.
     */
}
