<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.model.Empleado" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Ficha del Empleado</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f4f7f6; text-align: center; }
        .info-container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); display: inline-block; text-align: left; width: 380px; }
        .info-group { margin-bottom: 15px; border-bottom: 1px solid #eee; padding-bottom: 8px; }
        .info-label { font-weight: bold; color: #555; display: block; font-size: 13px; margin-bottom: 3px; }
        .info-value { color: #000; font-size: 16px; }
        .btn { display: inline-block; background-color: #0275d8; color: white; padding: 10px 20px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; margin-top: 15px; width: 100%; text-align: center; text-decoration: none; box-sizing: border-box; }
        .btn-cancel { background-color: #aaa; margin-top: 5px; }
        .section-title { font-size: 14px; color: #666; margin-top: 25px; margin-bottom: 15px; border-bottom: 1px solid #ddd; padding-bottom: 5px; font-weight: bold; text-transform: uppercase; }
    </style>
</head>
<body>

    <div class="info-container">
        <!-- Usamos directamente el objeto del request sin declararlo en el head -->
        <% if (request.getAttribute("empleado") != null) {
            Empleado emp = (Empleado) request.getAttribute("empleado");
        %>
            <h2>Ficha del Empleado #<%= emp.id() %></h2>

            <!-- DATOS PERSONALES -->
            <div class="info-group">
                <span class="info-label">Nombre Completo:</span>
                <span class="info-value"><%= emp.nombre() %> <%= emp.primerApellido() %> <%= emp.segundoApellido() %></span>
            </div>

            <div class="info-group">
                <span class="info-label">Género:</span>
                <span class="info-value"><%= emp.genero() != null ? emp.genero() : "N/A" %></span>
            </div>

            <div class="info-group">
                <span class="info-label">Fecha de Alta:</span>
                <span class="info-value"><%= emp.fechaAlta() != null ? emp.fechaAlta() : "N/A" %></span>
            </div>

            <div class="info-group">
                <span class="info-label">Departamento Asignado:</span>
                <span class="info-value"><%= emp.departamento() != null ? emp.departamento().getNombre() : "Sin asignar" %></span>
            </div>

            <div class="info-group">
                <span class="info-label">Salario Bruto Anual:</span>
                <span class="info-value"><%= String.format("%.2f €", emp.salario()) %></span>
            </div>

            <!-- SECCIÓN: DATOS DE CONTACTO RELACIONALES -->
            <div class="section-title">Información de Contacto</div>

            <div class="info-group">
                <span class="info-label">Teléfono(s):</span>
                <!-- CORRECCIÓN: Separado por punto y coma (;) de forma segura -->
                <span class="info-value"><%= (emp.telefonos() != null && !emp.telefonos().isEmpty()) ? String.join("; ", emp.telefonos()) : "N/A" %></span>
            </div>

            <div class="info-group">
                <span class="info-label">Correo(s) Electrónico(s):</span>
                <!-- CORRECCIÓN: Separado por punto y coma (;) de forma segura -->
                <span class="info-value"><%= (emp.correos() != null && !emp.correos().isEmpty()) ? String.join("; ", emp.correos()) : "N/A" %></span>
            </div>

            <!-- BOTONES DE NAVEGACIÓN DIRECTA -->
            <a href="<%= request.getContextPath() %>/EmpleadoController?accion=editar&id=<%= emp.id() %>" class="btn">Editar Datos</a>
            <a href="<%= request.getContextPath() %>/MainController" class="btn btn-cancel">Volver al Listado</a>


        <% } else { %>
            <h2>Error de Lectura</h2>
            <p>No se han podido recuperar los datos del empleado solicitado de la base de datos.</p>
            <!-- Forma moderna estándar con Expresion Language en vez de Scriptlet -->
            <a href="${pageContext.request.contextPath}/MainController" class="btn btn-cancel">Volver al Listado</a>
        <% } %>
    </div>

</body>
</html>
