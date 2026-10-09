package modelo;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Operaciones CRUD sobre la tabla Usuarios (SQL Server).
 * Usa la conexión única del Singleton ConexionBD, que no se cierra aquí.
 */
public class UsuarioCrudDao {

    public static final String OK = "OK";
    public static final String DUPLICADO = "DUPLICADO";
    public static final String NO_ENCONTRADO = "NO_ENCONTRADO";
    public static final String ERROR_BD = "ERROR_BD";

    private static final String SELECT_BASE =
            "SELECT u.idUsuario, u.nombreUsuario, u.rol_id, r.nombre_rol, u.estado, "
          + "u.bloqueado, u.intentosFallidos, u.ultimoAcceso "
          + "FROM Usuarios u INNER JOIN rol r ON r.rol_id = u.rol_id ";

    /** Lista todos los usuarios. Devuelve null si hubo un error de base de datos. */
    public List<Usuario> listar() {
        Connection conn = ConexionBD.getInstancia().getConexion();
        if (conn == null) {
            return null;
        }
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY u.idUsuario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Busca un usuario por su id. Devuelve null si no existe o hay un error. */
    public Usuario buscarPorId(int idUsuario) {
        Connection conn = ConexionBD.getInstancia().getConexion();
        if (conn == null) {
            return null;
        }
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE u.idUsuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Busca un usuario por su nombre de usuario. Devuelve null si no existe o hay un error. */
    public Usuario buscarPorNombre(String nombreUsuario) {
        Connection conn = ConexionBD.getInstancia().getConexion();
        if (conn == null) {
            return null;
        }
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE u.nombreUsuario = ?")) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Crea un usuario con la contraseña encriptada (BCrypt). */
    public String crear(String nombreUsuario, String contrasena, int rolId,
                        String estado, String registradoPor) {
        Connection conn = ConexionBD.getInstancia().getConexion();
        if (conn == null) {
            return ERROR_BD;
        }
        String sql = "INSERT INTO Usuarios (nombreUsuario, contrasena, rol_id, permiso_id, estado, usuarioRegistro) "
                   + "VALUES (?, ?, ?, NULL, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, BCrypt.hashpw(contrasena, BCrypt.gensalt(10)));
            ps.setInt(3, rolId);
            ps.setString(4, estado);
            ps.setString(5, registradoPor);
            ps.executeUpdate();
            return OK;
        } catch (SQLException e) {
            return esDuplicado(e) ? DUPLICADO : errorBd(e);
        }
    }

    /**
     * Actualiza nombre, rol y estado. La contraseña solo se cambia si se envía una nueva.
     */
    public String editar(int idUsuario, String nombreUsuario, String contrasenaNueva,
                         int rolId, String estado) {
        Connection conn = ConexionBD.getInstancia().getConexion();
        if (conn == null) {
            return ERROR_BD;
        }
        boolean cambiarClave = contrasenaNueva != null && !contrasenaNueva.isBlank();
        String sql = "UPDATE Usuarios SET nombreUsuario = ?, rol_id = ?, estado = ?, "
                   + "fechaActualizacion = GETDATE()"
                   + (cambiarClave ? ", contrasena = ?" : "")
                   + " WHERE idUsuario = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, nombreUsuario);
            ps.setInt(i++, rolId);
            ps.setString(i++, estado);
            if (cambiarClave) {
                ps.setString(i++, BCrypt.hashpw(contrasenaNueva, BCrypt.gensalt(10)));
            }
            ps.setInt(i, idUsuario);
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return esDuplicado(e) ? DUPLICADO : errorBd(e);
        }
    }

    /** Elimina un usuario. */
    public String eliminar(int idUsuario) {
        return ejecutar("DELETE FROM Usuarios WHERE idUsuario = ?", idUsuario);
    }

    /** Quita el bloqueo y reinicia los intentos fallidos. */
    public String desbloquear(int idUsuario) {
        return ejecutar("UPDATE Usuarios SET bloqueado = 0, intentosFallidos = 0, "
                      + "fechaActualizacion = GETDATE() WHERE idUsuario = ?", idUsuario);
    }

    private String ejecutar(String sql, int idUsuario) {
        Connection conn = ConexionBD.getInstancia().getConexion();
        if (conn == null) {
            return ERROR_BD;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Timestamp ultimo = rs.getTimestamp("ultimoAcceso");
        return new Usuario(
                rs.getInt("idUsuario"),
                rs.getString("nombreUsuario"),
                rs.getInt("rol_id"),
                rs.getString("nombre_rol"),
                rs.getString("estado"),
                rs.getBoolean("bloqueado"),
                rs.getInt("intentosFallidos"),
                ultimo != null ? ultimo.toLocalDateTime() : null);
    }

    /** Errores 2627 y 2601 de SQL Server: valor duplicado en una columna única. */
    private boolean esDuplicado(SQLException e) {
        return e.getErrorCode() == 2627 || e.getErrorCode() == 2601;
    }

    private String errorBd(SQLException e) {
        e.printStackTrace();
        return ERROR_BD;
    }
}