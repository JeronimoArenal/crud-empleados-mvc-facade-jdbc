<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.model.Empleado" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Listado de Empleados</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f4f7f6; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; background: white; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
        tr:nth-child(even){ background-color: #f9f9f9; }
        .back-link { display: inline-block; margin-bottom: 20px; color: #4CAF50; text-decoration: none; font-weight: bold; }

        /* NUEVOS ESTILOS: Contenedor para alinear las acciones de forma limpia */
        .actions-container { display: flex; gap: 10px; }
        .btn-action { font-weight: bold; text-decoration: none; }
        .btn-view { color: #0275d8; }
        .btn-edit { color: #f0ad4e; }
        .btn-delete { color: #d9534f; }
    </style>
</head>
<body>

<%--                                ACLARACIONES
<% %> (Scriptlet de lógica): Sirve para escribir código Java puro que realiza acciones ocultas como
//(crear variables, hacer un if, arrancar un bucle for).
//<%= %> (Scriptlet de expresión): Sirve exclusivamente para pintar un valor en el HTML. Equivale a un System.out.print().
//¿Por qué hay llaves sueltas?
//Porque la estructura en Java es if (condicion) { ... } else { ... }.
//Como metemos etiquetas HTML en medio, tenemos que abrir <% solo para cerrar la llave } de Java y abrir el else.
 --%>
    <%
        List<Empleado> lista = (List<Empleado>) request.getAttribute("listaEmpleados");
        String errorMensaje = (String) request.getAttribute("errorMensaje");
        int totalRegistros = (lista != null) ? lista.size() : 0;        // Calculamos el tamaño total de la lista
    %>

    <a href="${pageContext.request.contextPath}/index.jsp" class="back-link">← Volver al Inicio</a>
    <a href="${pageContext.request.contextPath}/EmpleadoController">+ Añadir Empleado</a>
    <a href="AltaController">+ Alta Empleado</a>

    <div class="header-container">
        <!-- El contador dinámico queda integrado aquí dentro -->
        <h2 style="display: inline-block; margin: 0;">Registros obtenidos desde la Base Datos (<%= totalRegistros %>)</h2>
    </div>

    <%
        // 2º Evaluamos de forma directa el estado de los datos
        if (errorMensaje != null) {
    %>
            <p style="color: red; font-weight: bold;">Error de base de datos: <%= errorMensaje %></p>
    <%
        } else if (lista != null && !lista.isEmpty()) {
    %>
<table>
    <thead>
        <tr>
            <th>ID</th>
            <th>Nombre Completo</th>
            <th>Género</th>
            <th>Departamento</th>
            <th>Contacto</th>
            <th>Fecha Alta</th>
            <th>Salario</th>
            <th>Acciones</th> <!-- NUEVA CABECERA -->
        </tr>
    </thead>
    <tbody>
        <% for (Empleado emp : lista) { %>
            <tr>
                <td><%= emp.id() %></td>
                <td><%= emp.nombre() %> <%= emp.primerApellido() %> <%= emp.segundoApellido() %></td>
                <td><%= emp.genero() != null ? emp.genero() : "N/A" %></td>
                <td><%= emp.departamento() != null ? emp.departamento().getNombre() : "Sin asignar" %></td>

                <td>
                    <!-- Unimos todos los teléfonos y correos separados por coma -->
                    <strong>Tel:</strong>
                    <%= !emp.telefonos().isEmpty() ? String.join("; ", emp.telefonos()) : "N/A" %>
                    <br>
                    <strong>Email:</strong>
                    <%= !emp.correos().isEmpty() ? String.join("; ", emp.correos()) : "N/A" %>
                </td>

                <td><%= emp.fechaAlta() != null ? emp.fechaAlta() : "N/A" %></td>
                <td><%= String.format("%.2f", emp.salario()) %></td>

                <!-- Celda de Acciones unificadas -->
                <td>
                    <div class="actions-container">
                        <a class="btn-action btn-view" href="${pageContext.request.contextPath}/EmpleadoController?accion=ver&id=<%= emp.id() %>">Ver</a>

                        |
                        <a class="btn-action btn-edit" href="${pageContext.request.contextPath}/EmpleadoController?accion=editar&id=<%= emp.id() %>">Editar</a>
                        |
                        <a class="btn-action btn-delete" href="${pageContext.request.contextPath}/EmpleadoController?accion=eliminar&id=<%= emp.id() %>"
                           onclick="return confirm('¿Estás seguro de que deseas eliminar a <%= emp.nombre() %> y todos sus contactos?');">
                           Eliminar
                        </a>
                    </div>
                </td>
            </tr>
        <% } %>
    </tbody>
</table>

    <%
        } else {
    %>
            <p style="margin-top: 20px;">No se encontraron registros en la tabla de empleados.</p>
    <%
        }
    %>

</body>
</html>
