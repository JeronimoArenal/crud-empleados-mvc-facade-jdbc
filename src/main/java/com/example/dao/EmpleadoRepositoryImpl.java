package com.example.dao;

import com.example.config.DBConexion;
import com.example.controller.MainController;
import com.example.model.Departamento;
import com.example.model.Empleado;
import com.example.model.Genero;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmpleadoRepositoryImpl implements EmpleadoRepository {
    // La base de datos se maneja exclusivamente desde AQUÍ
    private final DBConexion dbConexion = new DBConexion("root", "Kose9suf");
    private static final Logger LOGGER = Logger.getLogger(EmpleadoRepositoryImpl.class.getName());


    //................................. checkConnection ..............................................
    @Override
    public boolean checkConnection() {
        try (Connection conn = dbConexion.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return false;
        }
    }

//    //................................. findAll ..............................................
//    @Override
//    public List<Empleado> findAll() {
//        List<Empleado> lista = new ArrayList<>();
////      String sql = "SELECT id, nombre, primerApellido, segundoApellido, fechaAlta, genero, salario FROM empleados";
////      String sql = "SELECT * FROM `empresa-crud-empleados`.empleados";
////        String sql = "SELECT * FROM empresa_crud_empleados.empleados";
//        String sql = "SELECT e.id, e.nombre, e.primerApellido, e.segundoApellido, e.fechaAlta, e.genero, e.salario, " +
//                "       d.id AS dept_id, d.nombre AS dept_nombre, " +
//                "       t.numero AS tel_numero, c.email AS corr_email " +
//                "FROM empresa_crud_empleados.empleados e " +
//                "LEFT JOIN empresa_crud_empleados.departamentos d ON e.departamentos_id = d.id " +
//                "LEFT JOIN empresa_crud_empleados.telefonos t ON e.id = t.empleados_id " +
//                "LEFT JOIN empresa_crud_empleados.correos c ON e.id = c.empleados_id";
//
//        try (Connection conn = dbConexion.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql);
//             ResultSet rs = ps.executeQuery()) {
//
//            while (rs.next()) {
//                // 1. Extraer datos primitivos y cadenas básicos
//                int id = rs.getInt("id");
//                String nombre = rs.getString("nombre");
//                String primerApellido = rs.getString("primerApellido");
//                String segundoApellido = rs.getString("segundoApellido");
//                double salario = rs.getDouble("salario");
//
//                // 2. Usar explícitamente java.sql.Date para la conversión limpia
//                Date sqlDate = rs.getDate("fechaAlta");
//                LocalDate fechaAlta = (sqlDate != null) ? sqlDate.toLocalDate() : null;
//
//                // 3. Convertir el texto de la base de datos al Enum Genero
//                String generoBD = rs.getString("genero");
//                Genero genero = (generoBD != null) ? Genero.valueOf(generoBD.trim().toUpperCase()) : null;
//
//                // 4. Instanciar el record Empleado usando su constructor completo.
//                Empleado emp = new Empleado(
//                        id,
//                        nombre,
//                        primerApellido,
//                        segundoApellido,
//                        genero,
//                        fechaAlta,
//                        salario,
//                        null,       // departamento
//                        List.of(),  // telefonos
//                        List.of()   // correos
//                );
//
//                lista.add(emp);
//            }
//        } catch (SQLException e) {
//            System.err.println("Error en Repository.findAll: " + e.getMessage());
//            LOGGER.log(Level.SEVERE, "Error al ejecutar la consulta findAll en empleados", e);
//        }
//        return lista;
//    }

    //................................. findAll ..............................................
    @Override
    public List<Empleado> findAll() {
        // Usamos un LinkedHashMap para mantener el orden en el que vienen los empleados desde la BD
        java.util.Map<Integer, Empleado> mapaEmpleados = new java.util.LinkedHashMap<>();

        // Consulta con LEFT JOIN para traer toda la información relacional de golpe
        String sql = "SELECT e.id, e.nombre, e.primerApellido, e.segundoApellido, e.fechaAlta, e.genero, e.salario, " +
                "       d.id AS dept_id, d.nombre AS dept_nombre, " +
                "       t.numero AS tel_numero, c.email AS corr_email " +
                "FROM empresa_crud_empleados.empleados e " +
                "LEFT JOIN empresa_crud_empleados.departamentos d ON e.departamentos_id = d.id " +
                "LEFT JOIN empresa_crud_empleados.telefonos t ON e.id = t.empleados_id " +
                "LEFT JOIN empresa_crud_empleados.correos c ON e.id = c.empleados_id";

        try (Connection conn = dbConexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");

                // Si el empleado aún no está en el mapa, lo creamos
                if (!mapaEmpleados.containsKey(id)) {
                    String nombre = rs.getString("nombre");
                    String primerApellido = rs.getString("primerApellido");
                    String segundoApellido = rs.getString("segundoApellido");
                    double salario = rs.getDouble("salario");

                    Date sqlDate = rs.getDate("fechaAlta");
                    LocalDate fechaAlta = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                    String generoBD = rs.getString("genero");
                    Genero genero = (generoBD != null) ? Genero.valueOf(generoBD.trim().toUpperCase()) : null;

                    // Extraer el objeto Departamento completo
                    int deptId = rs.getInt("dept_id");
                    String deptNombre = rs.getString("dept_nombre");
                    Departamento departamento = new Departamento(deptId, deptNombre);

                    // Inicializar el Record con listas mutables (ArrayList) para poder añadir datos en las siguientes filas
                    Empleado emp = new Empleado(
                            id, nombre, primerApellido, segundoApellido, genero, fechaAlta, salario,
                            departamento, new ArrayList<>(), new ArrayList<>()
                    );

                    mapaEmpleados.put(id, emp);
                }

                // Recuperamos el empleado del mapa para añadir sus contactos de esta fila
                Empleado empExistente = mapaEmpleados.get(id);

                // Añadir teléfono si existe en esta fila y no está repetido
                String tel = rs.getString("tel_numero");
                if (tel != null && !empExistente.telefonos().contains(tel)) {
                    empExistente.telefonos().add(tel);
                }

                // Añadir correo si existe en esta fila y no está repetido
                String correo = rs.getString("corr_email");
                if (correo != null && !empExistente.correos().contains(correo)) {
                    empExistente.correos().add(correo);
                }
            }

            LOGGER.info("Listado general cargado con éxito. Total empleados: " + mapaEmpleados.size());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error crítico en Repository.findAll relacional", e);
            throw new RuntimeException("Error al listar empleados con sus relaciones: " + e.getMessage());
        }

        // Convertimos los valores del mapa a una lista estándar de Java
        return new ArrayList<>(mapaEmpleados.values());
    }


    //................................. save ..............................................
    @Override
    public void save(Empleado empleado) {
        // Aseguramos meter explícitamente el campo fechaAlta en la consulta SQL
        String sqlEmpleado = "INSERT INTO empresa_crud_empleados.empleados " +
                "(nombre, primerApellido, segundoApellido, genero, fechaAlta, salario, departamentos_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        String sqlTelefono = "INSERT INTO empresa_crud_empleados.telefonos (numero, empleados_id) VALUES (?, ?)";
        String sqlCorreo = "INSERT INTO empresa_crud_empleados.correos (email, empleados_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = dbConexion.getConnection();
            // Desactivamos el auto-commit para que toda la inserción sea una transacción única
            conn.setAutoCommit(false);

            // VARIABLE DECLARADA AQUÍ (Para que sea visible en todo el método)
            int empleadoId = 0;

            // 1. Insertar el Empleado y recuperar la ID autogenerada de la base de datos
            try (PreparedStatement psEmp = conn.prepareStatement(sqlEmpleado, Statement.RETURN_GENERATED_KEYS)) {
                psEmp.setString(1, empleado.nombre());
                psEmp.setString(2, empleado.primerApellido());
                psEmp.setString(3, empleado.segundoApellido());
                psEmp.setString(4, empleado.genero() != null ? empleado.genero().name() : null);

                // SOLUCIÓN AL PROBLEMA: Pasamos la fecha seleccionada en el formulario JSP
                if (empleado.fechaAlta() != null) {
                    psEmp.setDate(5, java.sql.Date.valueOf(empleado.fechaAlta()));
                } else {
                    psEmp.setNull(5, Types.DATE);
                }

                psEmp.setDouble(6, empleado.salario());

                if (empleado.departamento() != null) {
                    psEmp.setInt(7, empleado.departamento().getId()); // Volvemos a .getId() porque es una clase estándar
                } else {
                    psEmp.setNull(7, Types.INTEGER);
                }

                psEmp.executeUpdate();

                // Recuperar la ID recién generada
                try (ResultSet generatedKeys = psEmp.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        empleadoId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Error al obtener la ID del empleado insertado.");
                    }
                }
            } // El PreparedStatement de empleado se cierra de forma segura aquí

            // 2. Insertar los teléfonos asociados al ID obtenido
            if (empleado.telefonos() != null && !empleado.telefonos().isEmpty()) {
                try (PreparedStatement psTel = conn.prepareStatement(sqlTelefono)) {
                    for (String tel : empleado.telefonos()) {
                        psTel.setString(1, tel);
                        psTel.setInt(2, empleadoId);
                        psTel.addBatch();
                    }
                    psTel.executeBatch();
                }
            }

            // 3. Insertar los correos asociados al ID obtenido
            if (empleado.correos() != null && !empleado.correos().isEmpty()) {
                try (PreparedStatement psCorr = conn.prepareStatement(sqlCorreo)) {
                    for (String correo : empleado.correos()) {
                        psCorr.setString(1, correo);
                        psCorr.setInt(2, empleadoId);
                        psCorr.addBatch();
                    }
                    psCorr.executeBatch();
                }
            }

            // Si todo ha ido bien, consolidamos la transacción completa
            conn.commit();
            LOGGER.info("Empleado guardado con éxito mediante save(). ID: " + empleadoId);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Cancelamos todo si ocurre algún error
                    LOGGER.warning("Se ha realizado un rollback en save() debido a un fallo.");
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Error crítico en Repository.save", e);
            throw new RuntimeException("Error al guardar el empleado en la base de datos: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close(); // Cerramos la conexión manualmente
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error al cerrar la conexión", e);
                }
            }
        }
    }


    //................................. findById ..............................................
    @Override
    public Empleado findById(int id) {
        Empleado empleado = null;

        // Consulta relacional filtrando por el ID del empleado
        String sql = "SELECT e.id, e.nombre, e.primerApellido, e.segundoApellido, e.fechaAlta, e.genero, e.salario, " +
                "       d.id AS dept_id, d.nombre AS dept_nombre, " +
                "       t.numero AS tel_numero, c.email AS corr_email " +
                "FROM empresa_crud_empleados.empleados e " +
                "LEFT JOIN empresa_crud_empleados.departamentos d ON e.departamentos_id = d.id " +
                "LEFT JOIN empresa_crud_empleados.telefonos t ON e.id = t.empleados_id " +
                "LEFT JOIN empresa_crud_empleados.correos c ON e.id = c.empleados_id " +
                "WHERE e.id = ?";

        try (Connection conn = dbConexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (empleado == null) {
                        String nombre = rs.getString("nombre");
                        String primerApellido = rs.getString("primerApellido");
                        String segundoApellido = rs.getString("segundoApellido");
                        double salario = rs.getDouble("salario");

                        Date sqlDate = rs.getDate("fechaAlta");
                        LocalDate fechaAlta = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                        String generoBD = rs.getString("genero");
                        Genero genero = (generoBD != null) ? Genero.valueOf(generoBD.trim().toUpperCase()) : null;

                        int deptId = rs.getInt("dept_id");
                        String deptNombre = rs.getString("dept_nombre");
                        Departamento departamento = new Departamento(deptId, deptNombre);

                        // Inicializamos listas mutables usando ArrayList
                        empleado = new Empleado(
                                id, nombre, primerApellido, segundoApellido, genero, fechaAlta, salario,
                                departamento, new ArrayList<>(), new ArrayList<>()
                        );
                    }

                    // Mapeamos los teléfonos asociados de forma segura
                    String tel = rs.getString("tel_numero");
                    if (tel != null && !empleado.telefonos().contains(tel)) {
                        empleado.telefonos().add(tel);
                    }

                    // Mapeamos los correos asociados de forma segura
                    String correo = rs.getString("corr_email");
                    if (correo != null && !empleado.correos().contains(correo)) {
                        empleado.correos().add(correo);
                    }
                }
            }
            LOGGER.info("Empleado encontrado con éxito. ID: " + id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error crítico en Repository.findById", e);
            throw new RuntimeException("Error al buscar empleado: " + e.getMessage());
        }
        return empleado;
    }

    //................................. update (Estilo Merge Sincronizado) ..............................................
    @Override
    public void update(Empleado emp) {
        String sqlEmpleado = "UPDATE empresa_crud_empleados.empleados SET nombre = ?, primerApellido = ?, segundoApellido = ?, genero = ?, fechaAlta = ?, salario = ?, departamentos_id = ? WHERE id = ?";

        String sqlCheckTel = "SELECT COUNT(*) FROM empresa_crud_empleados.telefonos WHERE empleados_id = ?";
        String sqlUpdateTel = "UPDATE empresa_crud_empleados.telefonos SET numero = ? WHERE empleados_id = ?";
        String sqlInsertTel = "INSERT INTO empresa_crud_empleados.telefonos (numero, empleados_id) VALUES (?, ?)";

        String sqlCheckCor = "SELECT COUNT(*) FROM empresa_crud_empleados.correos WHERE empleados_id = ?";
        String sqlUpdateCor = "UPDATE empresa_crud_empleados.correos SET email = ? WHERE empleados_id = ?";
        String sqlInsertCor = "INSERT INTO empresa_crud_empleados.correos (email, empleados_id) VALUES (?, ?)";

        try (Connection conn = dbConexion.getConnection()) {
            conn.setAutoCommit(false); // @Transactional manual

            try {
                // 1. Actualizar los datos base del empleado (Incluyendo la fecha)
                try (PreparedStatement psEmp = conn.prepareStatement(sqlEmpleado)) {
                    psEmp.setString(1, emp.nombre());
                    psEmp.setString(2, emp.primerApellido());
                    psEmp.setString(3, emp.segundoApellido());
                    psEmp.setString(4, emp.genero() != null ? emp.genero().name() : null);

                    // CORRECCIÓN CLAVE: Enviamos la fecha capturada al UPDATE
                    if (emp.fechaAlta() != null) {
                        psEmp.setDate(5, java.sql.Date.valueOf(emp.fechaAlta()));
                    } else {
                        psEmp.setNull(5, Types.DATE);
                    }

                    psEmp.setDouble(6, emp.salario());
                    psEmp.setInt(7, emp.departamento().getId());
                    psEmp.setInt(8, emp.id()); // Se desplaza a la posición 8
                    psEmp.executeUpdate();
                }

                // 2. Sincronizar Teléfono de forma segura (UPDATE o INSERT según corresponda)
                if (!emp.telefonos().isEmpty() && emp.telefonos().get(0) != null && !emp.telefonos().get(0).isBlank()) {
                    String nuevoTelefono = emp.telefonos().get(0);
                    boolean existeRegistro;

                    try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckTel)) {
                        psCheck.setInt(1, emp.id());
                        try (ResultSet rs = psCheck.executeQuery()) {
                            existeRegistro = rs.next() && rs.getInt(1) > 0;
                        }
                    }

                    if (existeRegistro) {
                        try (PreparedStatement psUpd = conn.prepareStatement(sqlUpdateTel)) {
                            psUpd.setString(1, nuevoTelefono);
                            psUpd.setInt(2, emp.id());
                            psUpd.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement psIns = conn.prepareStatement(sqlInsertTel)) {
                            psIns.setString(1, nuevoTelefono);
                            psIns.setInt(2, emp.id());
                            psIns.executeUpdate();
                        }
                    }
                }

                // 3. Sincronizar Correo de forma segura (UPDATE o INSERT según corresponda)
                if (!emp.correos().isEmpty() && emp.correos().get(0) != null && !emp.correos().get(0).isBlank()) {
                    String nuevoCorreo = emp.correos().get(0);
                    boolean existeRegistro;

                    try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckCor)) {
                        psCheck.setInt(1, emp.id());
                        try (ResultSet rs = psCheck.executeQuery()) {
                            existeRegistro = rs.next() && rs.getInt(1) > 0;
                        }
                    }

                    if (existeRegistro) {
                        try (PreparedStatement psUpd = conn.prepareStatement(sqlUpdateCor)) {
                            psUpd.setString(1, nuevoCorreo);
                            psUpd.setInt(2, emp.id());
                            psUpd.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement psIns = conn.prepareStatement(sqlInsertCor)) {
                            psIns.setString(1, nuevoCorreo);
                            psIns.setInt(2, emp.id());
                            psIns.executeUpdate();
                        }
                    }
                }

                conn.commit(); // Éxito de la transacción
                LOGGER.info("¡ÉXITO! Empleado ID " + emp.id() + " y contactos sincronizados correctamente.");

            } catch (SQLException ex) {
                conn.rollback(); // Cancelamos todo si algo falla a mitad de camino
                throw ex;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error crítico en Repository.update relacional", e);
            throw new RuntimeException("Error al actualizar el empleado: " + e.getMessage());
        }
    }



}
