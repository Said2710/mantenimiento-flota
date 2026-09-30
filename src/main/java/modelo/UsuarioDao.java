/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDao {

    /**
     * Valida el acceso del usuario y controla el bloqueo por 3 intentos fallidos.
     * @return Retorna un texto con el estado del resultado: "EXITO", "BLOQUEADO", "MAX_INTENTOS", "ERROR_PASS", "NO_EXISTE"
     */
    public String validarLogin(String username, String password) {
        String sqlCheck = "SELECT intentos_fallidos, estado, password FROM usuario WHERE username = ?";
        
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int intentos = rs.getInt("intentos_fallidos");
                    String estado = rs.getString("estado");
                    String passBD = rs.getString("password");
                    
                    // 1. Verificar si la cuenta ya está bloqueada previamente
                    if ("BLOQUEADO".equals(estado)) {
                        return "BLOQUEADO";
                    }
                    
                    // 2. Validar si la contraseña es correcta
                    if (passBD.equals(password)) {
                        // Contraseña correcta: reiniciamos los intentos fallidos a 0
                        resetearIntentos(username);
                        return "EXITO";
                    } else {
                        // Contraseña incorrecta: aumentamos en 1 los intentos fallidos
                        intentos++;
                        if (intentos >= 3) {
                            bloquearUsuario(username);
                            return "MAX_INTENTOS"; // Llegó a 3 intentos y se bloquea
                        } else {
                            actualizarIntentos(username, intentos);
                            return "ERROR_PASS"; // Contraseña incorrecta pero aún tiene intentos
                        }
                    }
                } else {
                    return "NO_EXISTE"; // El usuario no está registrado en la base de datos
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en la validación del login: " + e.getMessage());
            return "ERROR_BD";
        }
    }

    private void actualizarIntentos(String username, int intentos) {
        String sql = "UPDATE usuario SET intentos_fallidos = ? WHERE username = ?";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, intentos);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void bloquearUsuario(String username) {
        String sql = "UPDATE usuario SET intentos_fallidos = 3, estado = 'BLOQUEADO' WHERE username = ?";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetearIntentos(String username) {
        String sql = "UPDATE usuario SET intentos_fallidos = 0, estado = 'ACTIVO' WHERE username = ?";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
