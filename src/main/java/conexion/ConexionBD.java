package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Configura conexiones a Azure SQL Server.
 * La instancia es única, pero cada llamada entrega una conexión nueva.
 */
public final class ConexionBD {

    private static final String URL_POR_DEFECTO =
            "jdbc:sqlserver://sql-flota-said-2711.database.windows.net:1433;"
            + "database=db-mantenimiento-flota;encrypt=true;"
            + "trustServerCertificate=false;loginTimeout=30;";
    private static final String USUARIO_POR_DEFECTO = "adminflota";
    private static final ConexionBD INSTANCIA = new ConexionBD();

    private final String url;
    private final String usuario;

    private ConexionBD() {
        this.url = leer("DB_URL", URL_POR_DEFECTO);
        this.usuario = leer("DB_USER", USUARIO_POR_DEFECTO);
    }

    public static ConexionBD getInstancia() {
        return INSTANCIA;
    }

    /** Abre una conexión independiente para una operación. */
    public Connection getConexion() throws SQLException {
        String clave = System.getenv("DB_PASSWORD");
        if (clave == null || clave.isBlank()) {
            throw new SQLException("Falta configurar la variable de entorno DB_PASSWORD.");
        }

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver JDBC de SQL Server.", e);
        }

        return DriverManager.getConnection(url, usuario, clave);
    }

    private static String leer(String nombre, String valorPorDefecto) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? valorPorDefecto : valor;
    }
}
