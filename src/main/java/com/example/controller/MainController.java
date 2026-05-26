package com.example.controller;

import com.example.config.DBConexion;
import com.example.service.EmpleadoServiceImpl;
import com.example.service.IEmpleadoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/MainController")
public class MainController extends HttpServlet{

    private final DBConexion dbConexion = new DBConexion("root", "Kose9suf");   // Instanciamos el objeto de configuración con sus credenciales
    private final IEmpleadoService empleadoService = new EmpleadoServiceImpl(dbConexion);       // Le pasamos la 'dbConexion' al constructor del servicio


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        // El try-with-resources aquí asegura que el PrintWriter se cierre correctamente
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Test Conexión</title></head>");
            out.println("<body>");
            out.println("<h2>Estado de la conexión:</h2>");

            // Delegamos la validación a la capa de servicio
            boolean isOK = empleadoService.isConnectionOK();

            if (isOK) {
                out.println("<h1 style='color:green;'>¡Conexión JDBC exitosa con MySQL en Docker!</h1>");
                out.println("<p>La conexión se abrió y cerró correctamente en el servidor.</p>");
            } else {
                out.println("<h1 style='color:red;'>Error al conectar a la base de datos</h1>");
                out.println("<p>Revisa la consola del servidor (Tomcat) para ver el log detallado del error.</p>");
            }

            out.println("</body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
