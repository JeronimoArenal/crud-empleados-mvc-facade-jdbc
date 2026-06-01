package com.example.dao;

import com.example.config.DBConexion;
import com.example.controller.MainController;
import com.example.model.Departamento;
import com.example.model.Empleado;
import com.example.model.Genero;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    //................................. findAll ..............................................
    @Override
    public List<Empleado> findAll() {
        // Usamos un LinkedHashMap para mantener el orden en el que vienen los empleados desde la BD. Ademas nos permite agrupar
        // las múltiples filas de contactos (teléfonos y correos) de un mismo empleado en un único objeto,
        Map<Integer, Empleado> mapaEmpleados = new LinkedHashMap<>();

        // Consulta con LEFT JOIN para traer toda la información de los empleados incluidos los que no estan completos
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

                // Si el empleado aún no está en el mapa, lo creamos asegurando que sólo existe una vez
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
    //Si utilizamos throws Exception, y lo implementamos en otro obligamos a añadir tambien throws Exceptio al método
    public void save(Empleado empleado) {
        String sqlEmpleado = "INSERT INTO empresa_crud_empleados.empleados " +
                "(nombre, primerApellido, segundoApellido, genero, fechaAlta, salario, departamentos_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        String sqlTelefono = "INSERT INTO empresa_crud_empleados.telefonos (numero, empleados_id) VALUES (?, ?)";
        String sqlCorreo = "INSERT INTO empresa_crud_empleados.correos (email, empleados_id) VALUES (?, ?)";

        /**
         * Utilizamos Prepared Statements(Objeto de JDBC que precompila la consulta en la base de datos usando marcadores ?)
         * como defensa de los ataques de inyección de SQL.
         * Separan la parte fija de la consulta de los parámetros que recibe la misma.
         * El rendimiento es muy similar al de los procedimientos almacenados porque, una vez que se ejecuta la consulta,
         * el componente analizador u optimizador de consultas no tiene que analizar nuevamente el plan de ejecución
         * de la misma. Esta es compilada y guardada en el servidor, de forma tal que la próxima vez solamente
         * hay que pasarle los parámetros variables para ejecutarla, logrando que la ejecución sea lo más rápida,
         * eficiente y segura posible.
         */

        Connection conn = null;     //Inicializamos la variable
        try {
            conn = dbConexion.getConnection();
            // 1º Iniciamos la transacción (Todo se guarda o nada se guarda)
            conn.setAutoCommit(false);

            int empleadoId = 0;

            // 2º Insertamos el empleado usando PreparedStatement
            try (PreparedStatement psEmp = conn.prepareStatement(sqlEmpleado, Statement.RETURN_GENERATED_KEYS)) {
                psEmp.setString(1, empleado.nombre());
                psEmp.setString(2, empleado.primerApellido());
                psEmp.setString(3, empleado.segundoApellido());
                psEmp.setString(4, empleado.genero() != null ? empleado.genero().name() : null);

                if (empleado.fechaAlta() != null) {
                    psEmp.setDate(5, Date.valueOf(empleado.fechaAlta()));
                } else {
                    psEmp.setNull(5, Types.DATE);       //Java le avisa al motor SQL que marque la celda de Fecha como NULL
                }

                psEmp.setDouble(6, empleado.salario());

                if (empleado.departamento() != null) {
                    psEmp.setInt(7, empleado.departamento().getId());
                } else {
                    psEmp.setNull(7, java.sql.Types.INTEGER);   //Java le avisa al motor SQL que marque la celda int como NULL
                }

                //ejecutamos con Update porque no devuelve un conjunto de resultados, sino la cantidad de filas afectadas
                psEmp.executeUpdate();

                // 3º Recuperamos el ID autogenerado del empleado
                try (ResultSet generatedKeys = psEmp.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        empleadoId = generatedKeys.getInt(1);       //Asignamos el Id a la variable empeadoId;
                    } else {
                        throw new SQLException("No se pudo obtener el ID del empleado.");
                    }
                }
            }

            // 4º Insertamos los teléfonos de golpe usando Batch
            if (empleado.telefonos() != null && !empleado.telefonos().isEmpty()) {
                try (PreparedStatement psTel = conn.prepareStatement(sqlTelefono)) {
                    for (String telefono : empleado.telefonos()) {
                        psTel.setString(1, telefono);
                        psTel.setInt(2, empleadoId);
                        psTel.addBatch(); // Se acumula en memoria
                    }
                    psTel.executeBatch(); // Se envía un único viaje a la base de datos
                }
            }

            // 5º Insertamos los correos de golpe usando Batch
            if (empleado.correos() != null && !empleado.correos().isEmpty()) {
                try (PreparedStatement psCorreo = conn.prepareStatement(sqlCorreo)) {
                    for (String correo : empleado.correos()) {
                        psCorreo.setString(1, correo);
                        psCorreo.setInt(2, empleadoId);
                        psCorreo.addBatch(); // Se acumula en memoria
                    }
                    psCorreo.executeBatch(); // Se envía todo junto de una vez a la base de datos
                }
            }

            // 6º Si todo ha ido bien, confirmamos los cambios en la BD
            conn.commit();

        } catch (SQLException e) {
            // Si algo falla, cancelamos absolutamente todo para no dejar datos huérfanos
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Error crítico al guardar el empleado y sus datos", e);
        } finally {
            // Cerramos la conexión de forma segura
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //................................. findById ..............................................
    @Override
    public Empleado findById(int id) {
        Empleado empleado = null;

        // Consulta filtrando por el ID del empleado
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

//    //................................. update ..............................................
//    @Override
//    public void update(Empleado emp) {
//        String sqlEmpleado = "UPDATE empresa_crud_empleados.empleados SET nombre = ?, primerApellido = ?, segundoApellido = ?, genero = ?, fechaAlta = ?, salario = ?, departamentos_id = ? WHERE id = ?";
//
//        // 1. Buscaremos qué teléfonos y correos tiene actualmente en la base de datos
//        String sqlGetTels = "SELECT numero FROM empresa_crud_empleados.telefonos WHERE empleados_id = ?";
//        String sqlGetCors = "SELECT email FROM empresa_crud_empleados.correos WHERE empleados_id = ?";
//
//        // 2. Operaciones diferenciales
//        String sqlInsertTel = "INSERT INTO empresa_crud_empleados.telefonos (numero, empleados_id) VALUES (?, ?)";
//        String sqlDeleteTel = "DELETE FROM empresa_crud_empleados.telefonos WHERE numero = ? AND empleados_id = ?";
//
//        String sqlInsertCor = "INSERT INTO empresa_crud_empleados.correos (email, empleados_id) VALUES (?, ?)";
//        String sqlDeleteCor = "DELETE FROM empresa_crud_empleados.correos WHERE email = ? AND empleados_id = ?";
//
//        try (Connection conn = dbConexion.getConnection()) {
//            //Iniciamos la transaccion
//            conn.setAutoCommit(false);
//
//            try {
//                // --- ACTUALIZAR EMPLEADO ---
//                try (PreparedStatement psEmp = conn.prepareStatement(sqlEmpleado)) {
//                    psEmp.setString(1, emp.nombre());
//                    psEmp.setString(2, emp.primerApellido());
//                    psEmp.setString(3, emp.segundoApellido());
//                    psEmp.setString(4, emp.genero() != null ? emp.genero().name() : null);
//
//                    if (emp.fechaAlta() != null) {
//                        psEmp.setDate(5, java.sql.Date.valueOf(emp.fechaAlta()));
//                    } else {
//                        psEmp.setNull(5, java.sql.Types.DATE);
//                    }
//
//                    psEmp.setDouble(6, emp.salario());
//                    if (emp.departamento() != null) {
//                        psEmp.setInt(7, emp.departamento().getId());
//                    } else {
//                        psEmp.setNull(7, java.sql.Types.INTEGER);
//                    }
//                    psEmp.setInt(8, emp.id());
//                    psEmp.executeUpdate();
//                }
//
//                // --- SINCRONIZAR TELÉFONOS (Diferencial) ---
//                java.util.Set<String> telefonosBD = new java.util.HashSet<>();
//                try (PreparedStatement psGetTel = conn.prepareStatement(sqlGetTels)) {
//                    psGetTel.setInt(1, emp.id());
//                    try (ResultSet rs = psGetTel.executeQuery()) {
//                        while (rs.next()) { telefonosBD.add(rs.getString("numero")); }
//                    }
//                }
//
//                java.util.List<String> telefonosNuevos = emp.telefonos() != null ? emp.telefonos() : new java.util.ArrayList<>();
//
//                // Insertar los que vienen en el objeto pero NO estaban en la BD
//                try (PreparedStatement psInsTel = conn.prepareStatement(sqlInsertTel)) {
//                    boolean tieneInserts = false;
//                    for (String tel : telefonosNuevos) {
//                        if (tel != null && !tel.isBlank() && !telefonosBD.contains(tel)) {
//                            psInsTel.setString(1, tel);
//                            psInsTel.setInt(2, emp.id());
//                            psInsTel.addBatch();
//                            tieneInserts = true;
//                        }
//                    }
//                    if (tieneInserts) psInsTel.executeBatch();
//                }
//
//                // Borrar los que estaban en la BD pero el usuario quitó en la interfaz
//                try (PreparedStatement psDelTel = conn.prepareStatement(sqlDeleteTel)) {
//                    boolean tieneDeletes = false;
//                    for (String telBD : telefonosBD) {
//                        if (!telefonosNuevos.contains(telBD)) {
//                            psDelTel.setString(1, telBD);
//                            psDelTel.setInt(2, emp.id());
//                            psDelTel.addBatch();
//                            tieneDeletes = true;
//                        }
//                    }
//                    if (tieneDeletes) psDelTel.executeBatch();
//                }
//
//                // --- SINCRONIZAR CORREOS (Diferencial) ---
//                java.util.Set<String> correosBD = new java.util.HashSet<>();
//                try (PreparedStatement psGetCor = conn.prepareStatement(sqlGetCors)) {
//                    psGetCor.setInt(1, emp.id());
//                    try (ResultSet rs = psGetCor.executeQuery()) {
//                        while (rs.next()) { correosBD.add(rs.getString("email")); }
//                    }
//                }
//
//                java.util.List<String> correosNuevos = emp.correos() != null ? emp.correos() : new java.util.ArrayList<>();
//
//                // Insertar correos nuevos
//                try (PreparedStatement psInsCor = conn.prepareStatement(sqlInsertCor)) {
//                    boolean tieneInserts = false;
//                    for (String cor : correosNuevos) {
//                        if (cor != null && !cor.isBlank() && !correosBD.contains(cor)) {
//                            psInsCor.setString(1, cor);
//                            psInsCor.setInt(2, emp.id());
//                            psInsCor.addBatch();
//                            tieneInserts = true;
//                        }
//                    }
//                    if (tieneInserts) psInsCor.executeBatch();
//                }
//
//                // Borrar correos removidos
//                try (PreparedStatement psDelCor = conn.prepareStatement(sqlDeleteCor)) {
//                    boolean tieneDeletes = false;
//                    for (String corBD : correosBD) {
//                        if (!correosNuevos.contains(corBD)) {
//                            psDelCor.setString(1, corBD);
//                            psDelCor.setInt(2, emp.id());
//                            psDelCor.addBatch();
//                            tieneDeletes = true;
//                        }
//                    }
//                    if (tieneDeletes) psDelCor.executeBatch();
//                }
//
//                conn.commit();
//                LOGGER.info("¡ÉXITO! Sincronización diferencial completada para el empleado ID " + emp.id());
//
//            } catch (SQLException ex) {
//                conn.rollback();
//                throw ex;
//            }
//        } catch (SQLException e) {
//            LOGGER.log(Level.SEVERE, "Error en update diferencial", e);
//            throw new RuntimeException("Error al actualizar el empleado: " + e.getMessage());
//        }
//    }

    //................................. actualizar ..............................................
    @Override
    public void update(Empleado empleado) {
        String sqlUpdateEmpleado = "UPDATE empresa_crud_empleados.empleados SET " +
                "nombre = ?, primerApellido = ?, segundoApellido = ?, genero = ?, fechaAlta = ?, salario = ?, departamentos_id = ? " +
                "WHERE id = ?";
        String sqlInsertTelefono = "INSERT INTO empresa_crud_empleados.telefonos (numero, empleados_id) VALUES (?, ?)";
        String sqlInsertCorreo = "INSERT INTO empresa_crud_empleados.correos (email, empleados_id) VALUES (?, ?)";
        String sqlDeleteTelefonos = "DELETE FROM empresa_crud_empleados.telefonos WHERE empleados_id = ?";
        String sqlDeleteCorreos = "DELETE FROM empresa_crud_empleados.correos WHERE empleados_id = ?";

        Connection conn = null;
        try {
            conn = dbConexion.getConnection();
            conn.setAutoCommit(false); // Iniciamos transacción de esta manera no guarda nada hasta que demos la orden

            // Actualizamos tabla principal
            try (PreparedStatement psEmp = conn.prepareStatement(sqlUpdateEmpleado)) {
                psEmp.setString(1, empleado.nombre());
                psEmp.setString(2, empleado.primerApellido());
                psEmp.setString(3, empleado.segundoApellido());
                psEmp.setString(4, empleado.genero() != null ? empleado.genero().name() : null);
                psEmp.setDate(5, empleado.fechaAlta() != null ? Date.valueOf(empleado.fechaAlta()) : null);
                psEmp.setDouble(6, empleado.salario());
                if (empleado.departamento() != null) psEmp.setInt(7, empleado.departamento().getId()); else psEmp.setNull(7, Types.INTEGER);
                psEmp.setInt(8, empleado.id());
                psEmp.executeUpdate();
            }

            // Preparamos nuevos datos en memoria (Si hay duplicados, saltará el catch AQUÍ antes de borrar)
            List<String> telfs = empleado.telefonos();
            List<String> mails = empleado.correos();

            // Limpiamos datos antiguos si las listas traen contenidio
            if (telfs != null && !telfs.isEmpty()) {
                try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteTelefonos)) { psDel.setInt(1, empleado.id()); psDel.executeUpdate(); }
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsertTelefono)) {
                    for (String tel : telfs) { psIns.setString(1, tel); psIns.setInt(2, empleado.id()); psIns.addBatch(); }
                    psIns.executeBatch();
                }
            }
            if (mails != null && !mails.isEmpty()) {
                try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteCorreos)) { psDel.setInt(1, empleado.id()); psDel.executeUpdate(); }
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsertCorreo)) {
                    for (String mail : mails) { psIns.setString(1, mail); psIns.setInt(2, empleado.id()); psIns.addBatch(); }
                    psIns.executeBatch();
                }
            }

            conn.commit(); // Todo ha ido perfecto, guardamos físicamente
            LOGGER.info("Empleado ID " + empleado.id() + " actualizado con éxito.");
        //Si la DB detecta duplicado, salta el bloque haciendo el rollBack
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { LOGGER.severe(ex.getMessage()); } }

            // Mensaje si hay un error duplicado (UNIQUE)
            String msg = e.getMessage();
            if (msg.contains("Duplicate entry")) {
                throw new RuntimeException(msg.contains("telefonos") ? "El número de teléfono ya existe en el sistema." : "El correo electrónico ya existe en el sistema.");
            }
            throw new RuntimeException("Error en la actualización: " + msg);
        } finally {
            if (conn != null) { try { conn.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); } }
        }
    }


    //................................. delete ..............................................
    @Override
    public void delete(int id) {
        //Borrado con Integridad Referencial. Primero se borran los hijos, despues los padres, ya que no se permite hijos huerfanos
        String sqlDeleteTels = "DELETE FROM empresa_crud_empleados.telefonos WHERE empleados_id = ?";
        String sqlDeleteCors = "DELETE FROM empresa_crud_empleados.correos WHERE empleados_id = ?";
        String sqlDeleteEmp = "DELETE FROM empresa_crud_empleados.empleados WHERE id = ?";

        Connection conn = null;
        try {
            conn = dbConexion.getConnection();
            conn.setAutoCommit(false); // Transacción para asegurar que se borra todo o nada

            // 1. Borrar Teléfonos
            try (PreparedStatement psTel = conn.prepareStatement(sqlDeleteTels)) {
                psTel.setInt(1, id);
                psTel.executeUpdate();
            }

            // 2. Borrar Correos
            try (PreparedStatement psCor = conn.prepareStatement(sqlDeleteCors)) {
                psCor.setInt(1, id);
                psCor.executeUpdate();
            }

            // 3. Borrar Empleado
            try (PreparedStatement psEmp = conn.prepareStatement(sqlDeleteEmp)) {
                psEmp.setInt(1, id);
                int filasAfectadas = psEmp.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new SQLException("No se encontró el empleado con ID: " + id);
                }
            }

            conn.commit();
            LOGGER.info("Empleado y sus contactos eliminados con éxito. ID: " + id);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Error crítico al eliminar el empleado", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
