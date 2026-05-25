<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Mi Página JSP</title>
</head>
<body>
<!-- Conjuntamente con MVC seutiliza el patron Facade, que es un patrón que divide al proyecto en capas, que pueden facilmente
sustituibles. y que cada capa ocualta la anterior.
Es común que un proyecto tenga las sisguientes capas
Cada capa es un paquete, y cada paquete tiene una interface, y una implementacion, de tal forma que si queremos cambiar
la implementacion de una capa, no afectaria a las otras capas, ya que cada capa solo conoce la interfaz de la capa anterior,
y no su implementacion.
1º DAO.- que sería el modelo y donde se implementa el acceso a las bases de datos.
2º Service .-
3º Controllers .- que puede haber varios
4º Implementaciones .-
5º Model .-

-->

<!-- Para agregar codigo de Java en el documento html se utiliza el lenguaje de scripting llamado scrpitlet -->
    <h2 style="color: green"> <%= "Hello World!. We are looking for the old JSP (Java Server Pages)" %></h2>
<!-- 1º Creamos una lista inmutable -->
<%
List <String> nombres = List.of("Jeronimo", "Juan", "Maria");
%>
    <ul>
    <!--3º  Abrimos el bucle en Java -->
    <% for (String nombre : nombres) { %>
    <!-- 4º Inyectamos el nombre con una expresion java. utilizamos = para mostrar el resultado de una variable -->
        <li><%= nombre %></li>
    <!-- 5º Abrimos otro scrpitlet sólo para cerrar la llave del bucle for -->
    <% } %>
    </ul>
</body>
</html>