package modelo;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReglaMantenimientoRepositoryJdbc implements ReglaMantenimientoRepository {

    private static final String SELECT_BASE =
            "SELECT r.idRegla, r.idComponente, c.nombre AS nombreComponente, r.nombre, "
            + "r.tipoMetrica, r.intervalo, r.descripcion, r.activo, r.fechaCreacion "
            + "FROM dbo.ReglaMantenimiento r "
            + "INNER JOIN dbo.Componente c ON c.idComponente = r.idComponente ";

    @Override
    public List<ReglaMantenimiento> listar() {
        List<ReglaMantenimiento> reglas = new ArrayList<>();
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY c.nombre, r.nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) reglas.add(mapear(rs));
            return reglas;
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public ReglaMantenimiento buscarPorId(int idRegla) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE r.idRegla = ?")) {
            ps.setInt(1, idRegla);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public String crear(ReglaMantenimiento regla) {
        String sql = "INSERT INTO dbo.ReglaMantenimiento "
                + "(idComponente, nombre, tipoMetrica, intervalo, descripcion, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            asignar(ps, regla);
            ps.executeUpdate();
            return OK;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    @Override
    public String editar(ReglaMantenimiento regla) {
        String sql = "UPDATE dbo.ReglaMantenimiento SET idComponente = ?, nombre = ?, "
                + "tipoMetrica = ?, intervalo = ?, descripcion = ?, activo = ? WHERE idRegla = ?";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            asignar(ps, regla);
            ps.setInt(7, regla.getIdRegla());
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    @Override
    public String desactivar(int idRegla) {
        String sql = "UPDATE dbo.ReglaMantenimiento SET activo = 0 WHERE idRegla = ?";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRegla);
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    private void asignar(PreparedStatement ps, ReglaMantenimiento regla) throws SQLException {
        ps.setInt(1, regla.getIdComponente());
        ps.setNString(2, regla.getNombre());
        ps.setNString(3, regla.getTipoMetrica());
        ps.setBigDecimal(4, regla.getIntervalo());
        if (regla.getDescripcion() == null || regla.getDescripcion().isBlank()) {
            ps.setNull(5, java.sql.Types.NVARCHAR);
        } else {
            ps.setNString(5, regla.getDescripcion());
        }
        ps.setBoolean(6, regla.isActivo());
    }

    private ReglaMantenimiento mapear(ResultSet rs) throws SQLException {
        Timestamp fecha = rs.getTimestamp("fechaCreacion");
        return new ReglaMantenimiento(rs.getInt("idRegla"), rs.getInt("idComponente"),
                rs.getNString("nombreComponente"), rs.getNString("nombre"),
                rs.getNString("tipoMetrica"), rs.getBigDecimal("intervalo"),
                rs.getNString("descripcion"), rs.getBoolean("activo"),
                fecha == null ? null : fecha.toLocalDateTime());
    }

    private String errorBd(SQLException e) {
        registrarError(e);
        return ERROR_BD;
    }

    private void registrarError(SQLException e) {
        System.err.println("Error de base de datos (código " + e.getErrorCode() + ").");
    }
}
