package modelo;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RegistroOperacionRepositoryJdbc implements RegistroOperacionRepository {

    private static final String SELECT_BASE =
            "SELECT r.idRegistro, r.idVehiculo, v.placa, r.fechaHora, "
            + "r.kilometraje, r.horasMotor, r.observacion, r.idUsuarioRegistro, "
            + "u.nombreUsuario AS usuarioRegistro FROM dbo.RegistroOperacion r "
            + "INNER JOIN dbo.Vehiculo v ON v.idVehiculo = r.idVehiculo "
            + "INNER JOIN dbo.Usuarios u ON u.idUsuario = r.idUsuarioRegistro ";

    @Override
    public List<RegistroOperacion> listar() {
        List<RegistroOperacion> registros = new ArrayList<>();
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE
                     .replace("SELECT r.idRegistro", "SELECT TOP (100) r.idRegistro")
                     + "ORDER BY r.fechaHora DESC, r.idRegistro DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) registros.add(mapear(rs));
            return registros;
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public RegistroOperacion ultimoPorVehiculo(int idVehiculo) {
        String sql = SELECT_BASE + "WHERE r.idVehiculo = ? "
                + "ORDER BY r.fechaHora DESC, r.idRegistro DESC";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public String crear(RegistroOperacion registro) {
        String sql = "INSERT INTO dbo.RegistroOperacion "
                + "(idVehiculo, kilometraje, horasMotor, observacion, idUsuarioRegistro) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, registro.getIdVehiculo());
            ps.setBigDecimal(2, registro.getKilometraje());
            ps.setBigDecimal(3, registro.getHorasMotor());
            if (registro.getObservacion() == null || registro.getObservacion().isBlank()) {
                ps.setNull(4, java.sql.Types.NVARCHAR);
            } else {
                ps.setNString(4, registro.getObservacion());
            }
            ps.setInt(5, registro.getIdUsuarioRegistro());
            ps.executeUpdate();
            return OK;
        } catch (SQLException e) {
            registrarError(e);
            return ERROR_BD;
        }
    }

    private RegistroOperacion mapear(ResultSet rs) throws SQLException {
        RegistroOperacion registro = new RegistroOperacion();
        registro.setIdRegistro(rs.getInt("idRegistro"));
        registro.setIdVehiculo(rs.getInt("idVehiculo"));
        registro.setPlaca(rs.getString("placa"));
        Timestamp fecha = rs.getTimestamp("fechaHora");
        registro.setFechaHora(fecha == null ? null : fecha.toLocalDateTime());
        registro.setKilometraje(rs.getBigDecimal("kilometraje"));
        registro.setHorasMotor(rs.getBigDecimal("horasMotor"));
        registro.setObservacion(rs.getNString("observacion"));
        registro.setIdUsuarioRegistro(rs.getInt("idUsuarioRegistro"));
        registro.setUsuarioRegistro(rs.getString("usuarioRegistro"));
        return registro;
    }

    private void registrarError(SQLException e) {
        System.err.println("Error de base de datos (código " + e.getErrorCode() + ").");
    }
}
