package com.example.controller;

import com.example.config.ConexionDB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

@WebServlet("/probar-db")
public class ProbarConexionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><body>");
        out.println("<h2>Estado de la conexión:</h2>");

        // Intentar conectar usando nuestra clase ConexionDB
        try (Connection con = ConexionDB.getConexion()) {
            if (con != null && !con.isClosed()) {
                out.println("<h1 style='color:green;'>✅ ¡Conexión JDBC exitosa con MySQL en Docker!</h1>");
                out.println("<p>Catálogo/Base de datos: " + con.getCatalog() + "</p>");
            }
        } catch (Exception e) {
            out.println("<h1 style='color:red;'>❌ Error al conectar a la base de datos</h1>");
            out.println("<p>Detalle del error:</p>");
            out.println("<pre style='background:#f4f4f4; padding:10px;'>" + e.getMessage() + "</pre>");
            // Imprime la traza completa en la pestaña 'Run' / Consola de Tomcat en IntelliJ
            e.printStackTrace();
        }

        out.println("</body></html>");
    }
}
