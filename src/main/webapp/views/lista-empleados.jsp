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
    </style>
</head>
<body>

 <%--   <a href="views/index.jsp" class="back-link">← Volver al Inicio</a> --%>
    <h2>Registros obtenidos desde la Base de Datos</h2>

    <%
        // 1º Recuperamos la lista y los posibles errores directamente del request
        List<Empleado> lista = (List<Empleado>) request.getAttribute("listaEmpleados");
        String errorMensaje = (String) request.getAttribute("errorMensaje");

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
                        <th>Fecha Alta</th>
                        <th>Salario</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Empleado emp : lista) { %>
                        <tr>
                            <td><%= emp.id() %></td>
                            <td><%= emp.nombre() %> <%= emp.primerApellido() %> <%= emp.segundoApellido() %></td>
                            <td><%= emp.genero() != null ? emp.genero() : "N/A" %></td>
                            <td><%= emp.fechaAlta() != null ? emp.fechaAlta() : "N/A" %></td>
                            <td>$<%= emp.salario() %></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
    <%
        } else {
    %>
            <p>No se encontraron registros en la tabla de empleados.</p>
    <%
        }
    %>

</body>
</html>
