package modelo;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioDao {

    private static final int MAX_INTENTOS = 3;

    private record DatosLogin(int intentos, boolean bloqueado, String estado, String hash) { }

    /** Valida el acceso y actualiza el contador de intentos fallidos. */
    public String validarLogin(String username, String password) {
        try (Connection conn = ConexionBD.getInstancia().getConexion()) {
            DatosLogin datos = buscarUsuario(conn, username);
            if (datos == null) {
                return "NO_EXISTE";
            }
            if (datos.bloqueado()) {
                return "BLOQUEADO";
            }
            if (!"Activo".equals(datos.estado())) {
                return "INACTIVO";
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
                   + "FROM dbo.Usuarios WHERE nombreUsuario = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
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

    private boolean passwordCorrecta(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void registrarAcceso(Connection conn, String username) throws SQLException {
        ejecutarUpdate(conn,
                "UPDATE dbo.Usuarios SET intentosFallidos = 0, bloqueado = 0, "
                + "ultimoAcceso = GETDATE(), fechaActualizacion = GETDATE() "
                + "WHERE nombreUsuario = ?", username, null);
    }

    private void bloquearUsuario(Connection conn, String username) throws SQLException {
        ejecutarUpdate(conn,
                "UPDATE dbo.Usuarios SET intentosFallidos = ?, bloqueado = 1, "
                + "fechaActualizacion = GETDATE() WHERE nombreUsuario = ?",
                username, MAX_INTENTOS);
    }

    private void actualizarIntentos(Connection conn, String username, int intentos)
            throws SQLException {
        ejecutarUpdate(conn,
                "UPDATE dbo.Usuarios SET intentosFallidos = ?, fechaActualizacion = GETDATE() "
                + "WHERE nombreUsuario = ?", username, intentos);
    }

    private void ejecutarUpdate(Connection conn, String sql, String username, Integer intentos)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int indice = 1;
            if (intentos != null) {
                ps.setInt(indice++, intentos);
            }
            ps.setString(indice, username);
            ps.executeUpdate();
        }
    }
}
