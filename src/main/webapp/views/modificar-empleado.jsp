<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.model.Departamento" %>
<%@ page import="com.example.model.Empleado" %>
<%
    // 1. Recuperamos el objeto empleado que el servlet nos envía para editar
    Empleado emp = (Empleado) request.getAttribute("empleado");

    // 2. Extraemos de forma segura el teléfono y correo de las listas del Record
    String telefonoValue = (emp != null && emp.telefonos() != null && !emp.telefonos().isEmpty()) ? emp.telefonos().get(0) : "";
    String correoValue = (emp != null && emp.correos() != null && !emp.correos().isEmpty()) ? emp.correos().get(0) : "";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Modificar Empleado</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f4f7f6; text-align: center; }
        .form-container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); display: inline-block; text-align: left; width: 380px; }
        .form-group { margin-bottom: 15px; }
        label { display: block; font-weight: bold; margin-bottom: 5px; }
        input, select { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
        .btn { display: inline-block; background-color: #0275d8; color: white; padding: 10px 20px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; margin-top: 10px; width: 100%; text-align: center; text-decoration: none; }
        .btn-cancel { background-color: #aaa; margin-top: 5px; }
        .section-title { font-size: 14px; color: #666; margin-top: 20px; margin-bottom: 10px; border-bottom: 1px solid #eee; padding-bottom: 5px; font-weight: bold; text-transform: uppercase; }

        /* ESTILOS NUEVOS: Alineación limpia de los botones de opción */
        .radio-group { display: flex; gap: 15px; margin-top: 5px; }
        .radio-label { display: flex; align-items: center; gap: 5px; font-weight: normal; cursor: pointer; }
        .radio-group input[type="radio"] { width: auto; cursor: pointer; }
    </style>
</head>
<body>

    <div class="form-container">
        <h2>Modificar Empleado</h2>
        <form action="${pageContext.request.contextPath}/EmpleadoController" method="POST">

            <!-- CAMPOS OCULTOS DE CONTROL -->
            <input type="hidden" name="accion" value="modificar">
            <input type="hidden" name="id" value="<%= emp != null ? emp.id() : 0 %>">

            <!-- DATOS PERSONALES -->
            <div class="form-group">
                <label for="nombre">Nombre:</label>
                <input type="text" id="nombre" name="nombre" value="<%= emp != null ? emp.nombre() : "" %>" required>
            </div>

            <div class="form-group">
                <label for="primerApellido">Primer Apellido:</label>
                <input type="text" id="primerApellido" name="primerApellido" value="<%= emp != null ? emp.primerApellido() : "" %>" required>
            </div>

            <div class="form-group">
                <label for="segundoApellido">Segundo Apellido:</label>
                <input type="text" id="segundoApellido" name="segundoApellido" value="<%= emp != null ? emp.segundoApellido() : "" %>">
            </div>

            <!-- CAMBIO: Mapeo a botones de opción (Radio Buttons) con persistencia -->
            <div class="form-group">
                <label>Género:</label>
                <div class="radio-group">
                    <label class="radio-label">
                        <input type="radio" id="generoHombre" name="genero" value="HOMBRE" <%= (emp != null && emp.genero() != null && "HOMBRE".equals(emp.genero().name())) ? "checked" : "" %>> Hombre
                    </label>
                    <label class="radio-label">
                        <input type="radio" id="generoMujer" name="genero" value="MUJER" <%= (emp != null && emp.genero() != null && "MUJER".equals(emp.genero().name())) ? "checked" : "" %>> Mujer
                    </label>
                    <label class="radio-label">
                        <input type="radio" id="generoOtro" name="genero" value="OTRO" <%= (emp != null && emp.genero() != null && "OTRO".equals(emp.genero().name())) ? "checked" : "" %>> Otro
                    </label>
                </div>
            </div>

            <!-- CAMBIO: Ahora la fecha es visible y modificable, carga la fecha real del empleado -->
            <div class="form-group">
                <label for="fechaAlta">Fecha de Alta:</label>
                <input type="date" id="fechaAlta" name="fechaAlta" value="<%= emp != null ? emp.fechaAlta() : "" %>" required>
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
                            boolean esSuDepartamento = (emp != null && emp.departamento() != null && emp.departamento().getId() == dept.getId());
                    %>
                        <option value="<%= dept.getId() %>" <%= esSuDepartamento ? "selected" : "" %>><%= dept.getNombre() %></option>
                    <%
                        }
                    }
                    %>
                </select>
            </div>

            <div class="form-group">
                <label for="salario">Salario:</label>
                <input type="number" id="salario" name="salario" step="0.01" value="<%= emp != null ? emp.salario() : "" %>" required>
            </div>

            <!-- SECCIÓN: DATOS DE CONTACTO -->
            <div class="section-title">Información de Contacto</div>

            <div class="form-group">
                <label for="telefono">Teléfono:</label>
                <input type="tel" id="telefono" name="telefono" placeholder="Ej: 600112233" value="<%= telefonoValue %>" required>
            </div>

            <div class="form-group">
                <label for="correo">Correo Electrónico:</label>
                <input type="email" id="correo" name="correo" placeholder="Ej: nombre@empresa.com" value="<%= correoValue %>" required>
            </div>

            <button type="submit" class="btn">Actualizar Empleado</button>
            <a href="${pageContext.request.contextPath}/MainController" class="btn btn-cancel">Cancelar</a>
        </form>
    </div>

</body>
</html>
