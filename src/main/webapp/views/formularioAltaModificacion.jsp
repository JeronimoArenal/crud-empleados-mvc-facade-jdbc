<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.model.Empleado" %>
<%@ page import="com.example.model.Departamento" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Formulario</title>
    <style>
        .radio-group { display: flex; gap: 15px; margin-top: 5px; }
        .radio-label { display: flex; align-items: center; gap: 5px; font-weight: normal; cursor: pointer; }
        .form-group { margin-bottom: 15px; }
        label { display: block; font-weight: bold; margin-bottom: 5px; }
        .msg-alert { padding: 10px; margin-bottom: 15px; border-radius: 4px; font-weight: bold; }
    </style>
</head>
<body>
<h1>Formulario Alta/Modificación de empleado</h1>

<fieldset>
<legend>Formulario de Gestión de empleados</legend>

    <form action="${pageContext.request.contextPath}/AltaController" method="POST">
            <!-- DATOS PERSONALES -->
            <div class="form-group">
                <label for="nombre">Nombre:</label>
                <input type="text" id="nombre" name="nombre" required>
            </div>
            <div class="form-group">
                <label for="primerApellido">Primer Apellido:</label>
                <input type="text" id="primerApellido" name="primerApellido" required>
            </div>
            <div class="form-group">
                <label for="segundoApellido">Segundo Apellido:</label>
                <input type="text" id="segundoApellido" name="segundoApellido" placeholder="No es obligatorio...">
            </div>
            <div class="form-group">
                <label for="fechaAlta">Fecha de Alta:</label>
                <input type="date" id="fechaAlta" name="fechaAlta" required>
            </div>
            <div class="form-group">
                <label>Género:</label>
                <div class="radio-group">
                    <label class="radio-label">
                        <input type="radio" id="generoHombre" name="genero" value="HOMBRE" checked> Hombre
                    </label>
                    <label class="radio-label">
                        <input type="radio" id="generoMujer" name="genero" value="MUJER"> Mujer
                    </label>
                    <label class="radio-label">
                        <input type="radio" id="generoOtro" name="genero" value="OTRO"> Otro
                    </label>
                </div>
            </div>
            <div class="form-group">
                 <label for="salario">Salario:</label>
                   <input type="number" id="salario" name="salario" step="0.01" required>
            </div>

            <!-- ASIGNACIÓN OBLIGATORIA DE DEPARTAMENTO -->
            <div class="form-group">
                 <label for="departamentoId">Departamento:</label>
                <select id="departamentoId" name="departamentoId" required>
                    <option value="">-- Seleccione un departamento --</option>
                    <%
                    List<Departamento> departamentos = (List<Departamento>) request.getAttribute("listaDepartamentos");
                    if (departamentos != null) {
                        for (Departamento dept : departamentos) {
                    %>
                        <option value="<%= dept.getId() %>"><%= dept.getNombre() %></option>
                    <%
                        }
                    }
                    %>
                </select>
            </div>
                        <!-- SECCIÓN: DATOS DE CONTACTO -->
            <div class="section-title">Información de Contacto</div>

            <div class="form-group">
                  <label for="telefono">Numeros de Teléfono:</label>
                   <input type="tel" id="telefono" name="telefono" placeholder="Ej: 600112233" required>
            </div>

            <div class="form-group">
                 <label for="correo">Correos Electrónicos:</label>
                <input type="email" id="correo" name="correo" placeholder="Ej: nombre@empresa.com" required>
            </div>

            <br>
            <input type="submit" value ="Enviar">
    </form>
</fieldset>

</body>
</html>
