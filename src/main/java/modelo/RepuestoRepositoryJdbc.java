package modelo;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class RepuestoRepositoryJdbc implements RepuestoRepository {

    private static final String SELECT_BASE =
            "SELECT idRepuesto, codigo, nombre, descripcion, unidadMedida, stockMinimo, estado, "
            + "idUsuarioRegistro FROM dbo.Repuesto ";

    @Override
    public List<Repuesto> listar() {
        List<Repuesto> repuestos = new ArrayList<>();
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) repuestos.add(mapear(rs));
            return repuestos;
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public Repuesto buscarPorId(int idRepuesto) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE idRepuesto = ?")) {
            ps.setInt(1, idRepuesto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public String crear(Repuesto repuesto) {
        String sql = "INSERT INTO dbo.Repuesto (codigo, nombre, descripcion, unidadMedida, stockMinimo, "
                + "estado, fechaCreacion, idUsuarioRegistro) VALUES (?, ?, ?, ?, ?, ?, SYSDATETIME(), ?)";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, repuesto.getCodigo());
            ps.setString(2, repuesto.getNombre());
            setNullable(ps, 3, repuesto.getDescripcion());
            ps.setString(4, repuesto.getUnidadMedida());
            ps.setBigDecimal(5, repuesto.getStockMinimo());
            ps.setString(6, repuesto.getEstado());
            setNullable(ps, 7, repuesto.getIdUsuarioRegistro());
            ps.executeUpdate();
            return OK;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    @Override
    public String editar(Repuesto repuesto) {
        String sql = "UPDATE dbo.Repuesto SET codigo = ?, nombre = ?, descripcion = ?, unidadMedida = ?, "
                + "stockMinimo = ?, estado = ? WHERE idRepuesto = ?";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, repuesto.getCodigo());
            ps.setString(2, repuesto.getNombre());
            setNullable(ps, 3, repuesto.getDescripcion());
            ps.setString(4, repuesto.getUnidadMedida());
            ps.setBigDecimal(5, repuesto.getStockMinimo());
            ps.setString(6, repuesto.getEstado());
            ps.setInt(7, repuesto.getIdRepuesto());
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    @Override
    public String eliminar(int idRepuesto) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE dbo.Repuesto SET estado = N'Inactivo' WHERE idRepuesto = ?")) {
            ps.setInt(1, idRepuesto);
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    private Repuesto mapear(ResultSet rs) throws SQLException {
        Repuesto repuesto = new Repuesto();
        repuesto.setIdRepuesto(rs.getInt("idRepuesto"));
        repuesto.setCodigo(rs.getString("codigo"));
        repuesto.setNombre(rs.getString("nombre"));
        repuesto.setDescripcion(rs.getString("descripcion"));
        repuesto.setUnidadMedida(rs.getString("unidadMedida"));
        repuesto.setStockMinimo(rs.getBigDecimal("stockMinimo"));
        repuesto.setEstado(rs.getString("estado"));
        int idUsuario = rs.getInt("idUsuarioRegistro");
        repuesto.setIdUsuarioRegistro(rs.wasNull() ? null : idUsuario);
        return repuesto;
    }

    private void setNullable(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) ps.setNull(index, Types.NVARCHAR);
        else ps.setString(index, value);
    }

    private void setNullable(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }

    private String errorBd(SQLException e) {
        System.err.println("Error en Repuesto (código " + e.getErrorCode() + ").");
        return ERROR_BD;
    }

    private void registrarError(SQLException e) {
        System.err.println("Error en Repuesto (código " + e.getErrorCode() + ").");
    }
}
