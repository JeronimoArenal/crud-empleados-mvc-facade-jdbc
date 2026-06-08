package com.example.controller;

import com.example.model.Departamento;
import com.example.model.Empleado;
import com.example.service.DepartamentoService;
import com.example.service.DepartamentoServiceImpl;
import com.example.service.EmpleadoService;
import com.example.service.EmpleadoServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/DetallesController")
public class DetallesController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DetallesController.class.getName());
    private final EmpleadoService empleadoService = new EmpleadoServiceImpl();

    // ==========================================================================================
    // GET (Equivale a @GetMapping de Spring)
    // ==========================================================================================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 1. Capturamos el ID que viene de la URL
            int idEmpleado = Integer.parseInt(request.getParameter("id"));

            // 2. Buscamos el empleado único en la base de datos
            Empleado emp = empleadoService.buscarPorId(idEmpleado);

            // 3. ¡CORREGIDO!: Guardamos el objeto directo, SIN listas intermedias
            request.setAttribute("empleado", emp);

            LOGGER.info("Mostrando detalles del empleado ID: " + idEmpleado);

            // 4. Pasamos el control al JSP
            request.getRequestDispatcher("/views/detalles-contacto.jsp").forward(request, response);

        } catch (Exception e) {
            LOGGER.severe("Error al cargar los detalles: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/MainController");
        }
    }


}
