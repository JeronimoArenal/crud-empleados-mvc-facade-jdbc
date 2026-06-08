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
    //Switch Expressions
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) accion = "nuevo";

        switch (accion) {
            case "ver"      -> detalleEmpleado(request, response);
            case "editar"   -> formularioEditar(request, response);
            case "eliminar" -> eliminarEmpleado(request, response);
            case "nuevo"    -> formularioNuevo(request, response);
            default         -> formularioNuevo(request, response);
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
            case "modificar" -> actualizarEmpleado(request, response);
            case "crear"     -> guardarEmpleado(request, response);
            default          -> guardarEmpleado(request, response);
        }
    }

    // ==========================================================================================
    // MÉTODOS DE ACCIÓN GET (Manejo de vistas)
    // ==========================================================================================

    //................................. nuevoEmpelado ..............................................
    private void formularioNuevo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        cargarAtributosComunes(request);
        request.getRequestDispatcher("/views/nuevo-empleado.jsp").forward(request, response);
    }

    //................................. verEmpleado ..............................................
    private void detalleEmpleado(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Extraemos el ID del JSP que viene como String y convertimos a Integer
            int id = Integer.parseInt(request.getParameter("id"));

            // Buscamos el empleado usando el método findById
            Empleado emp = empleadoService.buscarPorId(id);

            // Guardamos el objeto empleado en el request para que el nuevo JSP pueda leerlo
            request.setAttribute("empleado", emp);

            // Transformamos las listas a cadenas separadas por ";" de forma segura antes de ir al JSP
            String telefonoValue = (emp != null && emp.telefonos() != null) ? String.join("; ", emp.telefonos()) : "";
            String correoValue = (emp != null && emp.correos() != null) ? String.join("; ", emp.correos()) : "";

            // Pasamos los textos formateados como atributos independientes
            request.setAttribute("telefonoTexto", telefonoValue);
            request.setAttribute("correoTexto", correoValue);

            // Redirigimos a la nueva vista de detalle
            request.getRequestDispatcher("/views/detalle-empleado.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.severe("Error al cargar la vista de detalle: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/MainController");
        }
    }

    //................................. EditarEmpleado ..............................................
    private void formularioEditar(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Empleado emp = empleadoService.buscarPorId(id);

            request.setAttribute("empleado", emp);
            cargarAtributosComunes(request);

            // Transformamos las listas a texto plano separado por ";" desde el Servlet
            String telefonoValue = (emp != null && emp.telefonos() != null) ? String.join("; ", emp.telefonos()) : "";
            String correoValue = (emp != null && emp.correos() != null) ? String.join("; ", emp.correos()) : "";

            // Los enviamos como atributos simples a la vista
            request.setAttribute("telefonoTexto", telefonoValue);
            request.setAttribute("correoTexto", correoValue);

            request.getRequestDispatcher("/views/modificar-empleado.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.severe("Error al cargar edición: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/MainController");
        }
    }


    // ==========================================================================================
    // MÉTODOS DE ACCIÓN POST
    // ==========================================================================================
    //................................. guardar ..............................................
    private void guardarEmpleado(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Intentamos leer la fecha enviada desde el JSP ("nuevo-empleado.jsp")
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

    //................................. actualizar ..............................................
    private void actualizarEmpleado(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int idEmpleado = 0;
        try {
            idEmpleado = Integer.parseInt(request.getParameter("id"));

            LocalDate fechaAlta;
            try {
                String fechaAltaStr = request.getParameter("fechaAlta");
                if (fechaAltaStr != null && !fechaAltaStr.isBlank()) {
                    fechaAlta = LocalDate.parse(fechaAltaStr);
                } else {
                    fechaAlta = empleadoService.buscarPorId(idEmpleado).fechaAlta();
                }
            } catch (Exception e) {
                fechaAlta = empleadoService.buscarPorId(idEmpleado).fechaAlta();
            }

            // Mapeamos el objeto con los inputs actuales que introdujo el usuario
            Empleado empleadoActualizado = mapearEmpleadoDesdeRequest(request, idEmpleado, fechaAlta);

            // Intentamos guardar. Si salta UNIQUE, irá al catch de abajo
            empleadoService.modificar(empleadoActualizado);

            LOGGER.info("Empleado actualizado con éxito.");
            response.sendRedirect(request.getContextPath() + "/MainController");

        } catch (Exception e) {
            LOGGER.severe("Error al procesar la actualización: " + e.getMessage());

            // CORRECCIÓN CRÍTICA: Recuperamos los textos tal y como los escribió el usuario para no perderlos
            String telefonoTexto = request.getParameter("telefono");
            String correoTexto = request.getParameter("correo");

            // Si por algún motivo falló el mapeo inicial, volvemos a buscar el empleado base de la BD
            Empleado empVuelco = null;
            try {
                if (idEmpleado > 0) {
                    empVuelco = empleadoService.buscarPorId(idEmpleado);
                }
            } catch (Exception ex) {
                LOGGER.severe("No se pudo re-buscar el empleado para el volcado: " + ex.getMessage());
            }

            // Volvemos a inyectar TODOS los atributos para que el JSP con scriptlets los repinte
            request.setAttribute("empleado", empVuelco);
            request.setAttribute("telefonoTexto", telefonoTexto != null ? telefonoTexto : "");
            request.setAttribute("correoTexto", correoTexto != null ? correoTexto : "");


            // Redirigimos al formulario manteniendo vivos los datos en el request
            redirigirConError(request, response, "/views/modificar-empleado.jsp", e);
        }
    }

    //................................. eliminar ..............................................
    private void eliminarEmpleado(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            empleadoService.eliminar(id); // O .delete(id) según tu interfaz
            LOGGER.info("Empleado ID " + id + " eliminado.");
        } catch (Exception e) {
            LOGGER.severe("Error al eliminar: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/MainController");
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

        String telefonoRaw = request.getParameter("telefono");
        String correoRaw = request.getParameter("correo");

        // Procesar teléfonos: cortamos por ";" y limpiamos espacios de cada uno
        List<String> telefonos = new java.util.ArrayList<>();
        if (telefonoRaw != null && !telefonoRaw.isBlank()) {
            for (String tel : telefonoRaw.split(";")) {
                if (!tel.trim().isEmpty()) {
                    telefonos.add(tel.trim());
                }
            }
        }

        // Procesar correos: cortamos por ";" y limpiamos espacios de cada uno
        List<String> correos = new java.util.ArrayList<>();
        if (correoRaw != null && !correoRaw.isBlank()) {
            for (String mail : correoRaw.split(";")) {
                if (!mail.trim().isEmpty()) {
                    correos.add(mail.trim());
                }
            }
        }

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
