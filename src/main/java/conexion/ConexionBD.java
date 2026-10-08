package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton que administra la conexión a la base de datos (SQL Server en Azure).
 * Solo existe una instancia en toda la aplicación.
 */
public final class ConexionBD {

    private static ConexionBD instancia;

    private final String url;
    private final String usuario;
    private final String clave;
    private Connection conexion;

    // Constructor privado: nadie puede crear otra instancia con "new"
    private ConexionBD() {
        this.url = leer("DB_URL",
                "jdbc:sqlserver://sql-flota-said-2711.database.windows.net:1433;"
                + "database=db-mantenimiento-flota;encrypt=true;"
                + "trustServerCertificate=false;loginTimeout=30;");
        this.usuario = leer("DB_USER", "adminflota");
        this.clave = leer("DB_PASSWORD", "");
    }

    /** Punto de acceso único a la instancia. */
    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    /** Devuelve una conexión válida; la reabre si se cerró o se cayó. */
    public synchronized Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
                cerrarSilenciosamente();
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                conexion = DriverManager.getConnection(url, usuario, clave);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("No se encontró el driver JDBC de SQL Server.");
            conexion = null;
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos.");
            e.printStackTrace();
            conexion = null;
        }
        return conexion;
    }

    /** Compatibilidad con el código anterior (se quitará más adelante). */
    public static Connection conectar() {
        return getInstancia().getConexion();
    }

    private void cerrarSilenciosamente() {
        try {
            if (conexion != null) {
                conexion.close();
            }
        } catch (SQLException ignorado) {
            // no hay nada que hacer
        }
        conexion = null;
    }

    /** Lee una variable de entorno; si no existe, usa el valor por defecto. */
    private static String leer(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return (valor != null && !valor.isBlank()) ? valor : porDefecto;
    }
}