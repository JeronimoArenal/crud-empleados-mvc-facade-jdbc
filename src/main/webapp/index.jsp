<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bienvenido - Gestión de Empleados</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f4f7f6; text-align: center; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); display: inline-block; }
        .btn { display: inline-block; background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold; margin-top: 20px; transition: background 0.3s; }
        .btn:hover { background-color: #45a049; }
    </style>
</head>
<body>
<!--
  Conjuntamente con MVC se utiliza el patrón Facade, que divide al proyecto en capas fácilmente sustituibles.
  Cada capa es un paquete que define una interfaz y su implementación.
  1º DAO (Repository): Acceso exclusivo a la base de datos (MySQL/Docker).
  2º Service (Business/Facade): Lógica de negocio y pasarela entre el controlador y los datos.
  3º Controllers (Servlets): Reciben peticiones HTTP, coordinan el flujo y eligen la vista.
  4º Model (Records): Estructuras de datos inmutables que viajan entre capas.
-->

    <div class="container">
        <h1>Bienvenido a la aplicación de gestión de empleados</h1>

        <div>
            <!-- Este enlace invoca al método doGet de tu MainController -->
            <a href="MainController" class="btn">Mostrar listado de empleados</a>
        </div>
    </div>

</body>
</html>
