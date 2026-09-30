/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    
    private static final String URL = "jdbc:postgresql://localhost:5432/db_mantenimiento_flota";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin123"; // Asegúrate de que sea tu contraseña actual

    public static Connection conectar() {
        Connection conexion = null;
        try {
            Class.forName("org.postgresql.Driver");
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("¡Conexión exitosa a PostgreSQL local!");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el Driver JDBC de PostgreSQL.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("--- ERROR SQL DETALLADO ---");
            e.printStackTrace(); // Esto imprimirá el motivo exacto en la consola de NetBeans
        }
        return conexion;
    }
}
