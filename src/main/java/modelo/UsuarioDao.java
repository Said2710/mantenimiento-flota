package modelo;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioDao {

    private static final int MAX_INTENTOS = 3;

    /** Datos del usuario que se necesitan para validar el login. */
    private record DatosLogin(int intentos, boolean bloqueado, String estado, String hash) { }

    /**
     * Valida el acceso del usuario y controla el bloqueo por 3 intentos fallidos.
     * @return "EXITO", "BLOQUEADO", "MAX_INTENTOS", "ERROR_PASS", "NO_EXISTE" o "ERROR_BD"
     */
    public String validarLogin(String username, String password) {
        // Conexión única del Singleton: se reutiliza y NO se cierra aquí
        Connection conn = ConexionBD.getInstancia().getConexion();
        if (conn == null) {
            return "ERROR_BD";
        }

        try {
            DatosLogin datos = buscarUsuario(conn, username);
            if (datos == null) {
                return "NO_EXISTE";
            }

            if (datos.bloqueado() || !"Activo".equals(datos.estado())) {
                return "BLOQUEADO";
            }

            if (passwordCorrecta(password, datos.hash())) {
                registrarAcceso(conn, username);
                return "EXITO";
            }

            int intentos = datos.intentos() + 1;
            if (intentos >= MAX_INTENTOS) {
                bloquearUsuario(conn, username);
                return "MAX_INTENTOS";
            }
            actualizarIntentos(conn, username, intentos);
            return "ERROR_PASS";

        } catch (SQLException e) {
            System.err.println("Error en la validación del login: " + e.getMessage());
            return "ERROR_BD";
        }
    }

    private DatosLogin buscarUsuario(Connection conn, String username) throws SQLException {
        String sql = "SELECT intentosFallidos, bloqueado, estado, contrasena "
                   + "FROM Usuarios WHERE nombreUsuario = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new DatosLogin(
                        rs.getInt("intentosFallidos"),
                        rs.getBoolean("bloqueado"),
                        rs.getString("estado"),
                        rs.getString("contrasena"));
            }
        }
    }

    /** Compara la contraseña escrita con el hash BCrypt guardado. */
    private boolean passwordCorrecta(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false; // el valor guardado no es un hash BCrypt válido
        }
    }

    private void registrarAcceso(Connection conn, String username) {
        ejecutarUpdate(conn,
            "UPDATE Usuarios SET intentosFallidos = 0, bloqueado = 0, "
          + "ultimoAcceso = GETDATE(), fechaActualizacion = GETDATE() WHERE nombreUsuario = ?",
            username, null);
    }

    private void bloquearUsuario(Connection conn, String username) {
        ejecutarUpdate(conn,
            "UPDATE Usuarios SET intentosFallidos = 3, bloqueado = 1, "
          + "fechaActualizacion = GETDATE() WHERE nombreUsuario = ?",
            username, null);
    }

    private void actualizarIntentos(Connection conn, String username, int intentos) {
        ejecutarUpdate(conn,
            "UPDATE Usuarios SET intentosFallidos = ?, fechaActualizacion = GETDATE() "
          + "WHERE nombreUsuario = ?",
            username, intentos);
    }

    /** Ejecuta un UPDATE; si "intentos" no es null, se usa como primer parámetro. */
    private void ejecutarUpdate(Connection conn, String sql, String username, Integer intentos) {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int i = 1;
            if (intentos != null) {
                pstmt.setInt(i++, intentos);
            }
            pstmt.setString(i, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}