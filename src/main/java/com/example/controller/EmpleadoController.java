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

@WebServlet("/EmpleadoController")
public class EmpleadoController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EmpleadoController.class.getName());
    private final EmpleadoService empleadoService = new EmpleadoServiceImpl();
    private final DepartamentoService departamentoService = new DepartamentoServiceImpl();

    // ==========================================================================================
    // ROUTER GET (Equivale a @GetMapping de Spring)
    // ==========================================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) accion = "nuevo";

        switch (accion) {
            case "editar":
                mostrarFormularioEditar(request, response);
                break;
            case "nuevo":
            default:
                mostrarFormularioNuevo(request, response);
                break;
        }
    }

    // ==========================================================================================
    // ROUTER POST (Equivale a @PostMapping de Spring)
    // ==========================================================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        if (accion == null) accion = "crear";

        switch (accion) {
            case "modificar":
                actualizarEmpleado(request, response);
                break;
            case "crear":
            default:
                guardarEmpleado(request, response);
                break;
        }
    }

    // ==========================================================================================
    // MÉTODOS DE ACCIÓN GET (Manejo de vistas)
    // ==========================================================================================

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        cargarAtributosComunes(request);
        request.getRequestDispatcher("/views/nuevo-empleado.jsp").forward(request, response);
    }

    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Empleado emp = empleadoService.buscarPorId(id);

            request.setAttribute("empleado", emp);
            cargarAtributosComunes(request);

            request.getRequestDispatcher("/views/modificar-empleado.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.severe("Error al cargar edición: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/MainController");
        }
    }

    // ==========================================================================================
    // MÉTODOS DE ACCIÓN POST
    // ==========================================================================================
    private void guardarEmpleado(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // CORRECCIÓN CLAVE: Intentamos leer la fecha enviada desde el JSP ("nuevo-empleado.jsp")
            LocalDate fechaAlta;
            try {
                String fechaAltaStr = request.getParameter("fechaAlta");
                if (fechaAltaStr != null && !fechaAltaStr.isBlank()) {
                    fechaAlta = LocalDate.parse(fechaAltaStr);
                } else {
                    fechaAlta = LocalDate.now(); // Plan B por seguridad si viene vacío
                }
            } catch (Exception e) {
                fechaAlta = LocalDate.now(); // Plan B si falla el formateo
            }

            // Pasamos la fecha real capturada en lugar de forzar siempre LocalDate.now()
            Empleado nuevoEmpleado = mapearEmpleadoDesdeRequest(request, 0, fechaAlta);
            empleadoService.guardar(nuevoEmpleado);

            LOGGER.info("Empleado creado con éxito.");
            response.sendRedirect(request.getContextPath() + "/MainController");
        } catch (Exception e) {
            redirigirConError(request, response, "/views/nuevo-empleado.jsp", e);
        }
    }

    private void actualizarEmpleado(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int idEmpleado = Integer.parseInt(request.getParameter("id"));

            // Tratamiento protector de la fecha para la edición
            LocalDate fechaAlta;
            try {
                String fechaAltaStr = request.getParameter("fechaAlta");
                if (fechaAltaStr != null && !fechaAltaStr.isBlank()) {
                    fechaAlta = LocalDate.parse(fechaAltaStr);
                } else {
                    // Si el input no viene, recuperamos la fecha que ya tenía guardada para no pisarla con nulo
                    fechaAlta = empleadoService.buscarPorId(idEmpleado).fechaAlta();
                }
            } catch (Exception e) {
                fechaAlta = empleadoService.buscarPorId(idEmpleado).fechaAlta();
            }

            Empleado empleadoModificar = mapearEmpleadoDesdeRequest(request, idEmpleado, fechaAlta);
            empleadoService.modificar(empleadoModificar);

            LOGGER.info("Empleado modificado con éxito: ID " + idEmpleado);
            response.sendRedirect(request.getContextPath() + "/MainController");
        } catch (Exception e) {
            redirigirConError(request, response, "/views/modificar-empleado.jsp", e);
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

    /**
     * Centraliza el mapeo de los inputs al objeto de dominio Empleado
     */
    private Empleado mapearEmpleadoDesdeRequest(HttpServletRequest request, int id, LocalDate fechaAlta) {
        String nombre = request.getParameter("nombre");
        String primerApellido = request.getParameter("primerApellido");
        String segundoApellido = request.getParameter("segundoApellido");
        double salario = Double.parseDouble(request.getParameter("salario"));
        Genero genero = Genero.valueOf(request.getParameter("genero").toUpperCase());

        int departamentoId = Integer.parseInt(request.getParameter("departamentoId"));
        Departamento depto = new Departamento(departamentoId, null);

        String telefono = request.getParameter("telefono");
        String correo = request.getParameter("correo");
        List<String> telefonos = (telefono != null && !telefono.isBlank()) ? List.of(telefono) : List.of();
        List<String> correos = (correo != null && !correo.isBlank()) ? List.of(correo) : List.of();

        return new Empleado(id, nombre, primerApellido, segundoApellido, genero, fechaAlta, salario, depto, telefonos, correos);
    }

    /**
     * Gestión unificada de fallos de validación o base de datos en las peticiones POST
     */
    private void redirigirConError(HttpServletRequest request, HttpServletResponse response, String vistaDestino, Exception e) throws ServletException, IOException {
        LOGGER.severe("Error al procesar operación: " + e.getMessage());
        request.setAttribute("errorMensaje", "Error: " + e.getMessage());
        cargarAtributosComunes(request);
        request.getRequestDispatcher(vistaDestino).forward(request, response);
    }
}
