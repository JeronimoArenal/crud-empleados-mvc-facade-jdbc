package com.example.controller;

import com.example.config.DBConexion;
import com.example.service.EmpleadoServiceImpl;
import com.example.service.EmpleadoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;


@WebServlet("/MainController")
public class MainController extends HttpServlet{

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    private final DBConexion dbConexion = new DBConexion("root", "Kose9suf");   // Instanciamos el objeto de configuración con sus credenciales
    private final EmpleadoService empleadoService = new EmpleadoServiceImpl(dbConexion);       // Le pasamos la 'dbConexion' al constructor del servicio


//    //............................ doGet ................................................
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
//
//        response.setContentType("text/html;charset=UTF-8");
//
//        // El try-with-resources aquí asegura que el PrintWriter se cierre correctamente
//        try (PrintWriter out = response.getWriter()) {
//            out.println("<!DOCTYPE html>");
//            out.println("<html>");
//            out.println("<head><title>Test Conexión</title></head>");
//            out.println("<body>");
//            out.println("<h2>Estado de la conexión:</h2>");
//
//            // Delegamos la validación a la capa de servicio
//            boolean itWorks = empleadoService.isConnectionOK();
//
//            if (itWorks) {
//                LOGGER.info("Conexion exitosa");
//                out.println("<h1 style='color:green;'>¡Conexión JDBC exitosa con MySQL en Docker!</h1>");
//                out.println("<p>La conexión se abrió y cerró correctamente en el servidor.</p>");
//            } else {
//                LOGGER.info("Error de conexion");
//                out.println("<h1 style='color:red;'>Error al conectar a la base de datos</h1>");
//                out.println("<p>Revisa la consola del servidor (Tomcat) para ver el log detallado del error.</p>");
//            }
//
//            out.println("</body></html>");
//        }
//    }

//    //............................ doGet ................................................
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
//        // Delegamos la validación a la capa de servicio
//        boolean itWorks = empleadoService.isConnectionOK();
//
//        if (itWorks) {
//            LOGGER.info("Conexion exitosa con MySQL en Docker.");
//            // Enviamos un código 204 (No Content): Indica éxito pero sin respuesta visual
//            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
//        } else {
//            LOGGER.severe("Error de conexion: Base de datos inaccesible.");
//            // Enviamos un código 500 (Internal Server Error) vacío para notificar el fallo de infraestructura
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//        }
//    }

//    //............................ doGet ................................................
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//
//        boolean itWorks = empleadoService.isConnectionOK();
//
//        if (itWorks) {
//            LOGGER.info("Conexion exitosa con MySQL en Docker. Saltando a la pagina principal.");
//            request.getRequestDispatcher("/index.jsp").forward(request, response);
//        } else {
//            LOGGER.severe("Error de conexion: Base de datos inaccesible. Saltando a la pagina de error.");
//
//            // Envía internamente a la vista de error (error.jsp)
//            request.getRequestDispatcher("/error.jsp").forward(request, response);
//        }
//    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
}
