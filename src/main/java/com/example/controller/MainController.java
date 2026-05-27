package com.example.controller;

import com.example.model.Empleado;
import com.example.service.EmpleadoService;
import com.example.service.EmpleadoServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/MainController")
public class MainController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    // Conectamos con la capa Service
    private final EmpleadoService empleadoService = new EmpleadoServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            // Ir directamente a buscar los empleados a la base de datos
            List<Empleado> lista = empleadoService.listarEmpleados();
            request.setAttribute("listaEmpleados", lista);
            LOGGER.info("Empleados cargados con éxito.");
        } catch (Exception e) {
            LOGGER.severe("Error al listar empleados: " + e.getMessage());
            request.setAttribute("errorMensaje", e.getMessage());
        }

        // Redirige de forma interna la solicitud hacia la vista JSP
        request.getRequestDispatcher("/views/lista-empleados.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Reservado para inserciones
    }
}
