package com.example.controller;

import com.example.model.Departamento;
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
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/AltaController")
public class AltaController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EmpleadoController.class.getName());
    private final EmpleadoService empleadoService = new EmpleadoServiceImpl();
    private final DepartamentoService departamentoService = new DepartamentoServiceImpl();

    //................................. doGet ..............................................
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Se invoca el método para poblar la lista de departamentos antes de ir a la vista
        cargarAtributosComunes(request);

        request.getRequestDispatcher("views/formularioAltaModificacion.jsp").forward(request, response);
    }

    // ==========================================================================================
    // MÉTODOS AUXILIARES (Equivalentes a @ModelAttribute / utilidades)
    // ==========================================================================================

    /**
     * Reutiliza la carga de datos necesarios para las vistas, similar a métodos @ModelAttribute
     */
    private void cargarAtributosComunes(HttpServletRequest request) {
        try {
            List<Departamento> departamentos = departamentoService.listarDepartamentos();
            request.setAttribute("listaDepartamentos", departamentos);
        } catch (Exception e) {
            LOGGER.severe("Error al cargar departamentos: " + e.getMessage());
            request.setAttribute("errorMensaje", "No se pudieron cargar los datos auxiliares.");
        }
    }

}
