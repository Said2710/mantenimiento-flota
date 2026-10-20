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

/** Acceso JDBC a dbo.Usuarios y dbo.rol en SQL Server. */
public class UsuarioRepositoryJdbc implements UsuarioRepository {

    private static final String SELECT_BASE =
            "SELECT u.idUsuario, u.nombreUsuario, u.rol_id, r.nombre_rol, u.estado, "
            + "u.bloqueado, u.intentosFallidos, u.ultimoAcceso "
            + "FROM dbo.Usuarios u INNER JOIN dbo.rol r ON r.rol_id = u.rol_id ";

    @Override
    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY u.idUsuario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapear(rs));
            }
            return usuarios;
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public Usuario buscarPorId(int idUsuario) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE u.idUsuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public Usuario buscarPorNombre(String nombreUsuario) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_BASE + "WHERE u.nombreUsuario = ?")) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public String crear(String nombreUsuario, String contrasena, int rolId,
                        String estado, String registradoPor) {
        String sql = "INSERT INTO dbo.Usuarios "
                + "(nombreUsuario, contrasena, rol_id, permiso_id, estado, usuarioRegistro) "
                + "VALUES (?, ?, ?, NULL, ?, ?)";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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

    @Override
    public String editar(int idUsuario, String nombreUsuario, String contrasenaNueva,
                         int rolId, String estado) {
        boolean cambiarClave = contrasenaNueva != null && !contrasenaNueva.isBlank();
        String sql = "UPDATE dbo.Usuarios SET nombreUsuario = ?, rol_id = ?, estado = ?, "
                + "fechaActualizacion = GETDATE()"
                + (cambiarClave ? ", contrasena = ?" : "")
                + " WHERE idUsuario = ?";

        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int indice = 1;
            ps.setString(indice++, nombreUsuario);
            ps.setInt(indice++, rolId);
            ps.setString(indice++, estado);
            if (cambiarClave) {
                ps.setString(indice++, BCrypt.hashpw(contrasenaNueva, BCrypt.gensalt(10)));
            }
            ps.setInt(indice, idUsuario);
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return esDuplicado(e) ? DUPLICADO : errorBd(e);
        }
    }

    @Override
    public String eliminar(int idUsuario) {
        return ejecutar("DELETE FROM dbo.Usuarios WHERE idUsuario = ?", idUsuario);
    }

    @Override
    public String desbloquear(int idUsuario) {
        return ejecutar("UPDATE dbo.Usuarios SET bloqueado = 0, intentosFallidos = 0, "
                + "fechaActualizacion = GETDATE() WHERE idUsuario = ?", idUsuario);
    }

    private String ejecutar(String sql, int idUsuario) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Timestamp ultimoAcceso = rs.getTimestamp("ultimoAcceso");
        return new Usuario(
                rs.getInt("idUsuario"),
                rs.getString("nombreUsuario"),
                rs.getInt("rol_id"),
                rs.getString("nombre_rol"),
                rs.getString("estado"),
                rs.getBoolean("bloqueado"),
                rs.getInt("intentosFallidos"),
                ultimoAcceso == null ? null : ultimoAcceso.toLocalDateTime());
    }

    private boolean esDuplicado(SQLException e) {
        return e.getErrorCode() == 2627 || e.getErrorCode() == 2601;
    }

    private String errorBd(SQLException e) {
        registrarError(e);
        return ERROR_BD;
    }

    private void registrarError(SQLException e) {
        System.err.println("Error de base de datos (código " + e.getErrorCode() + ").");
    }
}
