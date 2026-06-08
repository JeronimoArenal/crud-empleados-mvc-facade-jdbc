package com.example.dao;

import com.example.config.DBConexion;
import com.example.controller.MainController;
import com.example.model.Departamento;
import com.example.model.Empleado;
import com.example.model.Genero;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
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
        // Mapa para construir y asociar rápido los empleados en memoria. Problema con tamaño cuando hay muchos registros
        Map<Integer, Empleado> mapaEmpleados = new LinkedHashMap<>(50);

        // Tres consultas limpias, sin Joins cruzados de colecciones (Adiós producto cartesiano)
        String sqlEmpleados = "SELECT e.id, e.nombre, e.primerApellido, e.segundoApellido, e.fechaAlta, e.genero, e.salario, " +
                "       d.id AS dept_id, d.nombre AS dept_nombre " +
                "FROM empresa_crud_empleados.empleados e " +
                "INNER JOIN empresa_crud_empleados.departamentos d ON e.departamentos_id = d.id " +
                "ORDER BY e.id ASC";

        String sqlTodosTelefonos = "SELECT empleados_id, numero FROM empresa_crud_empleados.telefonos";
        String sqlTodosCorreos = "SELECT empleados_id, email FROM empresa_crud_empleados.correos";

        try (Connection conn = dbConexion.getConnection()) {

            // Cargar todos los empleados y sus departamentos (Una fila exacta por empleado)
            try (PreparedStatement ps = conn.prepareStatement(sqlEmpleados);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("id");
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

                    // Instanciamos el Record de forma limpia utilizando el patrón Builder
                    Empleado emp = Empleado.builder()
                            .id(id)
                            .nombre(nombre)
                            .primerApellido(primerApellido)
                            .segundoApellido(segundoApellido)
                            .genero(genero)
                            .fechaAlta(fechaAlta)
                            .salario(salario)
                            .departamento(departamento)
                            .telefonos(new ArrayList<>()) // Inicializamos la lista mutable vacía
                            .correos(new ArrayList<>())   // Inicializamos la lista mutable vacía
                            .build();

                    mapaEmpleados.put(id, emp);
                }
            }

            // Cargar todos los teléfonos de golpe y distribuirlos en el mapa
            try (PreparedStatement psTel = conn.prepareStatement(sqlTodosTelefonos);
                 ResultSet rsTel = psTel.executeQuery()) {

                while (rsTel.next()) {
                    int empId = rsTel.getInt("empleados_id");
                    String numero = rsTel.getString("numero");

                    Empleado emp = mapaEmpleados.get(empId);
                    if (emp != null && numero != null) {
                        emp.telefonos().add(numero); // Inyección directa y segura en la lista mutable interna
                    }
                }
            }

            //  Cargar todos los correos de golpe y distribuirlos en el mapa
            try (PreparedStatement psCorr = conn.prepareStatement(sqlTodosCorreos);
                 ResultSet rsCorr = psCorr.executeQuery()) {

                while (rsCorr.next()) {
                    int empId = rsCorr.getInt("empleados_id");
                    String email = rsCorr.getString("email");

                    Empleado emp = mapaEmpleados.get(empId);
                    if (emp != null && email != null) {
                        emp.correos().add(email); // Inyección directa y segura en la lista mutable interna
                    }
                }
            }

            LOGGER.info("Listado general cargado con éxito mediante consultas planas. Total: " + mapaEmpleados.size());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error crítico en Repository.findAll optimizado", e);
            throw new RuntimeException("Error al listar empleados: " + e.getMessage());
        }
        //Devolvemos al controlador la lista de empleados con sus datos
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

        //Inicializamos la variable de la conexión
        Connection conn = null;

        try {
            conn = dbConexion.getConnection();
            // Iniciamos la transacción (Todo se guarda o nada se guarda)
            conn.setAutoCommit(false);

            int empleadoId = 0;

            // Insertamos el empleado usando PreparedStatement
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

                // Recuperamos el ID autogenerado del empleado
                try (ResultSet generatedKeys = psEmp.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        empleadoId = generatedKeys.getInt(1);       //Asignamos el Id a la variable empeadoId;
                    } else {
                        throw new SQLException("No se pudo obtener el ID del empleado.");
                    }
                }
            }

            // Insertamos los teléfonos de golpe usando Batch
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

            // Insertamos los correos de golpe usando Batch
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
            //Lanzamos una nueva excepción
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

        // Consulta principal (Empleado + Departamento) -> Siempre devuelve 1 sola fila
        String sqlEmpleado = "SELECT e.id, e.nombre, e.primerApellido, e.segundoApellido, e.fechaAlta, e.genero, e.salario, " +
                "       d.id AS dept_id, d.nombre AS dept_nombre " +
                "FROM empresa_crud_empleados.empleados e " +
                "LEFT JOIN empresa_crud_empleados.departamentos d ON e.departamentos_id = d.id " +
                "WHERE e.id = ?";

        String sqlTelefonos = "SELECT numero FROM empresa_crud_empleados.telefonos WHERE empleados_id = ?";
        String sqlCorreos = "SELECT email FROM empresa_crud_empleados.correos WHERE empleados_id = ?";

        try (Connection conn = dbConexion.getConnection()) {

            // CONSULTA Datos Base
            try (PreparedStatement ps = conn.prepareStatement(sqlEmpleado)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int deptId = rs.getInt("dept_id");
                        String deptNombre = rs.getString("dept_nombre");
                        Departamento departamento = (deptNombre != null) ? new Departamento(deptId, deptNombre) : null;

                        Date sqlDate = rs.getDate("fechaAlta");
                        String generoBD = rs.getString("genero");

                        empleado = Empleado.builder()
                                .id(id)
                                .nombre(rs.getString("nombre"))
                                .primerApellido(rs.getString("primerApellido"))
                                .segundoApellido(rs.getString("segundoApellido"))
                                .salario(rs.getDouble("salario"))
                                .fechaAlta(sqlDate != null ? sqlDate.toLocalDate() : null)
                                .genero(generoBD != null ? Genero.valueOf(generoBD.trim().toUpperCase()) : null)
                                .departamento(departamento)
                                .telefonos(new ArrayList<>()) // Se llenarán abajo
                                .correos(new ArrayList<>())   // Se llenarán abajo
//                                .telefonos(listaTelefonos) // Se llenarán abajo
//                                .correos(listaCorreos)   // Se llenarán abajo
                                .build();
                    }
                }
            }

            // Si el empleado existe, buscamos sus colecciones de forma directa y limpia
            if (empleado != null) {
                // CONSULTA Teléfonos
                try (PreparedStatement psTel = conn.prepareStatement(sqlTelefonos)) {
                    psTel.setInt(1, id);
                    try (ResultSet rsTel = psTel.executeQuery()) {
                        while (rsTel.next()) {
                            empleado.telefonos().add(rsTel.getString("numero"));
                        }
                    }
                }
                // CONSULTA Correos
                try (PreparedStatement psCorr = conn.prepareStatement(sqlCorreos)) {
                    psCorr.setInt(1, id);
                    try (ResultSet rsCorr = psCorr.executeQuery()) {
                        while (rsCorr.next()) {
                            empleado.correos().add(rsCorr.getString("email"));
                        }
                    }
                }
                LOGGER.info("Empleado encontrado con éxito. ID: " + id);
            }

            /** SQLException es una excepción verificada (checked exception). Esto significa que el compilador
             * te obliga a gestionarla de forma obligatoria. Hay 2 opciones:
             * 1º Capturarla (bloque try-catch): Envolver el código que lanza la excepción en un bloque y gestionarla
             * localmente.
             * 2º Declararla en la firma (throws): Añadir throws SQLException en la firma de tu método para delegar la
             * responsabilidad a quien lo invoque.
             */
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error crítico en Repository.findById", e);
            throw new RuntimeException("Error al buscar empleado: " + e.getMessage());
        }
        return empleado;
    }

    //................................. update ..............................................
    @Override
    public void update(Empleado empleado) {
        String sqlUpdateEmpleado = "UPDATE empresa_crud_empleados.empleados SET " +
                "nombre = ?, primerApellido = ?, segundoApellido = ?, genero = ?, fechaAlta = ?, salario = ?, departamentos_id = ? " +
                "WHERE id = ?";

        // Usamos INSERT IGNORE para que si el dato ya existe tras el borrado temporal, ignora el error
        String sqlInsertTelefono = "INSERT IGNORE INTO empresa_crud_empleados.telefonos (numero, empleados_id) VALUES (?, ?)";
        String sqlInsertCorreo = "INSERT IGNORE INTO empresa_crud_empleados.correos (email, empleados_id) VALUES (?, ?)";

        String sqlDeleteTelefonos = "DELETE FROM empresa_crud_empleados.telefonos WHERE empleados_id = ?";
        String sqlDeleteCorreos = "DELETE FROM empresa_crud_empleados.correos WHERE empleados_id = ?";

        Connection conn = null;
        try {
            conn = dbConexion.getConnection();
            conn.setAutoCommit(false); // Le decim0s a la DB que no guarde nada hasta que la avisemos

            // Actualizar datos base del Record Empleado
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

            // Sincronización de Teléfonos (Limpieza completa primero)
            try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteTelefonos)) {
                psDel.setInt(1, empleado.id());
                psDel.executeUpdate();
            }

            List<String> telfs = empleado.telefonos();
            if (telfs != null && !telfs.isEmpty()) {
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsertTelefono)) {
                    for (String tel : telfs) {
                        psIns.setString(1, tel);
                        psIns.setInt(2, empleado.id());
                        psIns.addBatch();
                    }
                    psIns.executeBatch(); // Al usar INSERT IGNORE, los números mutados no colapsarán la transacción
                }
            }

            // Sincronización de Correos (Limpieza completa primero)
            try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteCorreos)) {
                psDel.setInt(1, empleado.id());
                psDel.executeUpdate();
            }

            List<String> mails = empleado.correos();
            if (mails != null && !mails.isEmpty()) {
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsertCorreo)) {
                    for (String mail : mails) {
                        psIns.setString(1, mail);
                        psIns.setInt(2, empleado.id());
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }

            // Si todo ha ido bien, grabamos los datos en MySQL
            conn.commit();
            LOGGER.info("Empleado ID " + empleado.id() + " actualizado con éxito con sincronización de colecciones.");

        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { LOGGER.severe(ex.getMessage()); } }

            String msg = e.getMessage();
            if (msg.contains("Duplicate entry")) {
                throw new RuntimeException(msg.contains("telefonos")
                        ? "El número de teléfono ya existe asignado a otro empleado."
                        : "El correo electrónico ya existe asignado a otro empleado.");
            }
            throw new RuntimeException("Error en la actualización: " + msg);
        } finally {
            if (conn != null) {
                try { conn.close();
                } catch (SQLException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
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

            // Borrar Teléfonos
            try (PreparedStatement psTel = conn.prepareStatement(sqlDeleteTels)) {
                psTel.setInt(1, id);
                psTel.executeUpdate();
            }

            // Borrar Correos
            try (PreparedStatement psCor = conn.prepareStatement(sqlDeleteCors)) {
                psCor.setInt(1, id);
                psCor.executeUpdate();
            }

            // Borrar Empleado
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
            throw new RuntimeException("Error crítico al eliminar el empleado: " + e.getMessage(), e);
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
