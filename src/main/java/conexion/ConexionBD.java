package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL = leer("DB_URL", "jdbc:postgresql://localhost:5432/db_mantenimiento_flota");
    private static final String USER = leer("DB_USER", "postgres");
    private static final String PASSWORD = leer("DB_PASSWORD", "admin123");

    /** Lee una variable de entorno; si no existe, usa el valor por defecto (local). */
    private static String leer(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return (valor != null && !valor.isBlank()) ? valor : porDefecto;
    }

    public static Connection conectar() {
        Connection conexion = null;
        try {
            Class.forName("org.postgresql.Driver");
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el Driver JDBC de PostgreSQL.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("--- ERROR SQL DETALLADO ---");
            e.printStackTrace();
        }
        return conexion;
    }
}