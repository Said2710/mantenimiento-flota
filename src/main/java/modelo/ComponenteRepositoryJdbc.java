package modelo;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ComponenteRepositoryJdbc implements ComponenteRepository {

    private static final String SELECT_BASE =
            "SELECT idComponente, nombre, descripcion, estado FROM dbo.Componente ";

    @Override
    public List<Componente> listar() {
        List<Componente> componentes = new ArrayList<>();
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                componentes.add(mapear(rs));
            }
            return componentes;
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public Componente buscarPorId(int idComponente) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE idComponente = ?")) {
            ps.setInt(1, idComponente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public String crear(Componente componente) {
        String sql = "INSERT INTO dbo.Componente (nombre, descripcion, estado) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, componente.getNombre());
            setNullable(ps, 2, componente.getDescripcion());
            ps.setString(3, componente.getEstado());
            ps.executeUpdate();
            return OK;
        } catch (SQLException e) {
            return esDuplicado(e) ? DUPLICADO : errorBd(e);
        }
    }

    @Override
    public String editar(Componente componente) {
        String sql = "UPDATE dbo.Componente SET nombre = ?, descripcion = ?, estado = ? "
                + "WHERE idComponente = ?";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, componente.getNombre());
            setNullable(ps, 2, componente.getDescripcion());
            ps.setString(3, componente.getEstado());
            ps.setInt(4, componente.getIdComponente());
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return esDuplicado(e) ? DUPLICADO : errorBd(e);
        }
    }

    @Override
    public String eliminar(int idComponente) {
        String sql = "UPDATE dbo.Componente SET estado = N'Inactivo' WHERE idComponente = ?";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idComponente);
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    private Componente mapear(ResultSet rs) throws SQLException {
        return new Componente(rs.getInt("idComponente"), rs.getString("nombre"),
                rs.getString("descripcion"), rs.getString("estado"));
    }

    private void setNullable(PreparedStatement ps, int indice, String valor) throws SQLException {
        if (valor == null || valor.isBlank()) {
            ps.setNull(indice, java.sql.Types.NVARCHAR);
        } else {
            ps.setString(indice, valor);
        }
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
