<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Bienvenido a la aplicacion de gestion de empleados</title>
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
    <h1> Bienvenido a la aplicacion de gestion de empleados</h1>

    <div>
    <a href="MainController">Mostrar listado de empleados</a>
    </div>

</body>
</html>