package com.example.controller;

import com.example.model.Departamento;
import com.example.model.Empleado;
import com.example.model.Genero;
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
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/AltaController")
public class AltaController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AltaController.class.getName());
    private final EmpleadoService empleadoService = new EmpleadoServiceImpl();
    private final DepartamentoService departamentoService = new DepartamentoServiceImpl();


    //................................. doGet ..............................................
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // Poblamos la lista de departamentos antes de ir a la vista
        cargarAtributosComunes(request);

        // Enviamos al usuario a la vista JSP
        request.getRequestDispatcher("views/formularioAltaModificacion.jsp").forward(request, response);
    }

    //................................. doPost ..............................................
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            String nombre = request.getParameter("nombre");

            // Tratamiento del primer apellido como opcional
            String primerApellido = request.getParameter("primerApellido");
            if (primerApellido == null) { primerApellido = ""; }

            String segundoApellido = request.getParameter("segundoApellido") == null ? "" : request.getParameter("segundoApellido");
            LocalDate fechaAlta = LocalDate.parse(request.getParameter("fechaAlta"));
            Genero genero = Genero.valueOf(request.getParameter("genero").toUpperCase());
            double salario = Double.parseDouble(request.getParameter("salario"));

            int departamentoId = Integer.parseInt(request.getParameter("departamentoId"));
            Departamento depto = new Departamento(departamentoId, null);

            String dirCorreoRecibidas = request.getParameter("correo");
            String dirTelefonoRecibidas = request.getParameter("telefono");

            // 1. Procesar varios correos
            List<String> correos;
            if (dirCorreoRecibidas != null && !dirCorreoRecibidas.isBlank()) {
                String[] arrayDirRecibidas = dirCorreoRecibidas.split(";");
                correos = List.of(arrayDirRecibidas);
            } else {
                correos = List.of();
            }

            // 2. Procesar teléfonos múltiples
            List<String> telefonos;
            if (dirTelefonoRecibidas != null && !dirTelefonoRecibidas.isBlank()) {
                String[] arrayTelRecibidas = dirTelefonoRecibidas.split(";");
                telefonos = List.of(arrayTelRecibidas);
            } else {
                telefonos = List.of();
            }

            // 3. CONSTRUCCIÓN CORRECTA CON BUILDER (Añadido .id(0))
            Empleado nuevoEmpleado = Empleado.builder()
                    .id(0)                  // <- SOLUCCIÓN: Forzamos el ID a 0 para altas nuevas
                    .nombre(nombre)
                    .primerApellido(primerApellido)
                    .segundoApellido(segundoApellido)
                    .fechaAlta(fechaAlta)
                    .genero(genero)
                    .salario(salario)
                    .departamento(depto)
                    .telefonos(telefonos)
                    .correos(correos)
                    .build();

            // 4. GUARDAR EN LA BASE DE DATOS
            empleadoService.guardar(nuevoEmpleado);

            // 5. REDIRECCIÓN TRAS ÉXITO
            response.sendRedirect(request.getContextPath() + "/AltaController?exito=true");

        } catch (Exception e) {
            LOGGER.severe("Error al procesar el formulario de alta: " + e.getMessage());
            request.setAttribute("errorMensaje", "Ocurrió un error al guardar el empleado.");
            cargarAtributosComunes(request);
            request.getRequestDispatcher("views/formularioAltaModificacion.jsp").forward(request, response);
        }
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
