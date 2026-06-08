<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.model.Departamento" %>
<%@ page import="com.example.model.Empleado" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Detalles de Contacto y Departamento</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f4f7f6; color: #333; }
        .container { max-width: 1000px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        h1, h2 { color: #2c3e50; border-bottom: 2px solid #4CAF50; padding-bottom: 8px; }
        .section { margin-bottom: 40px; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; background: #fff; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #4CAF50; color: white; font-weight: bold; }
        tr:nth-child(even) { background-color: #f9f9f9; }
        tr:hover { background-color: #f1f1f1; }
        .badge { display: inline-block; padding: 4px 8px; background: #e8f5e9; color: #2e7d32; border-radius: 4px; font-size: 0.9em; margin: 2px; }
        .btn-volver { display: inline-block; background-color: #7f8c8d; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; margin-top: 20px; transition: background 0.3s; }
        .btn-volver:hover { background-color: #95a5a6; }
    </style>
</head>
<body>

<div class="container">
    <h1>Panel de Información de Contacto</h1>

    <%
        // Recuperamos el record individual de manera limpia
        Empleado emp = (Empleado) request.getAttribute("empleado");

        if (emp != null) {
    %>
            <!-- SECCIÓN POR EMPLEADO -->
            <div class="section">
                <h2>Datos de: <%= emp.nombre() %> <%= emp.primerApellido() %></h2>
                <table>
                    <thead>
                        <tr>
                            <th>Departamento</th>
                            <th>Correos Electrónicos</th>
                            <th>Teléfonos</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <!-- DEPARTAMENTO DEL EMPLEADO -->
                           <td>
                               <% if (emp.departamento() != null) { %>
                                   <strong>ID:</strong> <%= emp.departamento().getId() %><br>
                                   <strong>Nombre:</strong> <%= emp.departamento().getNombre() %>
                               <% } else { %>
                                   <span style="color: #999; font-style: italic;">Sin asignar</span>
                               <% } %>
                           </td>

                            <!-- CORREOS -->
                            <td>
                                <% if (emp.correos() != null && !emp.correos().isEmpty()) {
                                        for (String correo : emp.correos()) { %>
                                            <span class="badge"><%= correo %></span>
                                <%      }
                                   } else { %>
                                        <span style="color: #999; font-style: italic;">Sin correo</span>
                                <% } %>
                            </td>

                            <!-- TELÉFONOS -->
                            <td>
                                <% if (emp.telefonos() != null && !emp.telefonos().isEmpty()) {
                                        for (String tlf : emp.telefonos()) { %>
                                            <span class="badge"><%= tlf %></span>
                                <%      }
                                   } else { %>
                                        <span style="color: #999; font-style: italic;">Sin teléfono</span>
                                <% } %>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
    <%
        } else {
    %>
            <p style="text-align: center; margin-top: 20px; color: red;">No se encontró la información del empleado.</p>
    <%
        }
    %>

    <a href="MainController" class="btn-volver">Volver al Inicio</a>
</div>

</body>
</html>
