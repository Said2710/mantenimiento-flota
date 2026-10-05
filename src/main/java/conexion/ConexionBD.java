package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton que administra la conexión a la base de datos.
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
        this.url = leer("DB_URL", "jdbc:postgresql://localhost:5432/db_mantenimiento_flota");
        this.usuario = leer("DB_USER", "postgres");
        this.clave = leer("DB_PASSWORD", "admin123");
    }

    /** Punto de acceso único a la instancia. */
    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    /** Devuelve una conexión válida; la reabre si se cerró o se cayó.
     * @return  */
    public synchronized Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
                cerrarSilenciosamente();
                Class.forName("org.postgresql.Driver");
                conexion = DriverManager.getConnection(url, usuario, clave);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("No se encontró el driver JDBC de PostgreSQL.");
            conexion = null;
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos.");
            conexion = null;
        }
        return conexion;
    }

    /** Compatibilidad con el código anterior (se quitará más adelante).
     * @return  */
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

    /** Lee una variable de entorno; si no existe, usa el valor local por defecto. */
    private static String leer(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return (valor != null && !valor.isBlank()) ? valor : porDefecto;
    }
}