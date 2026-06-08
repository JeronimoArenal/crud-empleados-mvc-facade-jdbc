<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.model.Departamento" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Añadir Empleado</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f4f7f6; text-align: center; }
        .form-container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); display: inline-block; text-align: left; width: 380px; }
        .form-group { margin-bottom: 15px; }
        label { display: block; font-weight: bold; margin-bottom: 5px; }
        input, select { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
        .btn { display: inline-block; background-color: #4CAF50; color: white; padding: 10px 20px; border: none; border-radius: 4px; font-weight: bold; cursor: pointer; margin-top: 10px; width: 100%; text-align: center; text-decoration: none; }
        .btn-cancel { background-color: #aaa; margin-top: 5px; }
        .section-title { font-size: 14px; color: #666; margin-top: 20px; margin-bottom: 10px; border-bottom: 1px solid #eee; padding-bottom: 5px; font-weight: bold; text-transform: uppercase; }
    </style>
</head>
<body>

    <div class="form-container">
        <h2>Nuevo Empleado</h2>
        <form action="${pageContext.request.contextPath}/EmpleadoController" method="POST">

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
                <input type="text" id="segundoApellido" name="segundoApellido">
            </div>

            <div class="form-group">
                <label for="genero">Género:</label>
                <select id="genero" name="genero">
                    <option value="HOMBRE">Hombre</option>
                    <option value="MUJER">Mujer</option>
                    <option value="OTRO">Otro</option>
                </select>
            </div>

            <div class="form-group">
                  <label for="fechaAlta">Fecha de Alta:</label>
                  <input type="date" id="fechaAlta" name="fechaAlta" required>
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
                    <!-- Uso de los métodos Get tradicionales -->
                    <option value="<%= dept.getId() %>"><%= dept.getNombre() %></option>
                        <%
                        }
                    }
                %>
                </select>
            </div>
            <div class="form-group">
                <label for="salario">Salario:</label>
                <input type="number" id="salario" name="salario" step="0.01" required>
            </div>

            <!-- SECCIÓN: DATOS DE CONTACTO -->
            <div class="section-title">Información de Contacto</div>

            <div class="form-group">
                <label for="telefono">Teléfono:</label>
                <input type="tel" id="telefono" name="telefono" placeholder="Ej: 600112233">
            </div>

            <div class="form-group">
                <label for="correo">Correo Electrónico:</label>
                <input type="text" id="correo" name="correo" placeholder="Ej: nombre@empresa.com">
            </div>

            <button type="submit" class="btn">Guardar Empleado</button>
            <a href="${pageContext.request.contextPath}/MainController" class="btn btn-cancel">Cancelar</a>
        </form>
    </div>

</body>
</html>
