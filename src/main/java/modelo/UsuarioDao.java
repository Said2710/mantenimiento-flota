package modelo;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDao {

    /**
     * Valida el acceso del usuario y controla el bloqueo por 3 intentos fallidos.
     * @return "EXITO", "BLOQUEADO", "MAX_INTENTOS", "ERROR_PASS", "NO_EXISTE" o "ERROR_BD"
     */
    public String validarLogin(String username, String password) {
        String sqlCheck = "SELECT intentos_fallidos, estado, password FROM usuario WHERE username = ?";

        // Conexión única del Singleton: se reutiliza y NO se cierra aquí
        Connection conn = ConexionBD.getInstancia().getConexion();
        if (conn == null) {
            return "ERROR_BD";
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int intentos = rs.getInt("intentos_fallidos");
                    String estado = rs.getString("estado");
                    String passBD = rs.getString("password");

                    // 1. Cuenta ya bloqueada
                    if ("BLOQUEADO".equals(estado)) {
                        return "BLOQUEADO";
                    }

                    // 2. Contraseña correcta
                    if (passBD.equals(password)) {
                        resetearIntentos(conn, username);
                        return "EXITO";
                    } else {
                        intentos++;
                        if (intentos >= 3) {
                            bloquearUsuario(conn, username);
                            return "MAX_INTENTOS";
                        } else {
                            actualizarIntentos(conn, username, intentos);
                            return "ERROR_PASS";
                        }
                    }
                } else {
                    return "NO_EXISTE";
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en la validación del login: " + e.getMessage());
            return "ERROR_BD";
        }
    }

    private void actualizarIntentos(Connection conn, String username, int intentos) {
        String sql = "UPDATE usuario SET intentos_fallidos = ? WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, intentos);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void bloquearUsuario(Connection conn, String username) {
        String sql = "UPDATE usuario SET intentos_fallidos = 3, estado = 'BLOQUEADO' WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetearIntentos(Connection conn, String username) {
        String sql = "UPDATE usuario SET intentos_fallidos = 0, estado = 'ACTIVO' WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}