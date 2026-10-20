package modelo;

import conexion.ConexionBD;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SolicitudServicioRepositoryJdbc implements SolicitudServicioRepository {

    private static final String SELECT_SOLICITUD =
            "SELECT s.idSolicitud, s.idCliente, c.razonSocial, s.fechaSolicitud, s.estado, "
            + "s.observacion, s.idVenta FROM dbo.SolicitudServicio s "
            + "JOIN dbo.Cliente c ON c.idCliente = s.idCliente ";

    @Override
    public List<SolicitudServicio> listar() {
        List<SolicitudServicio> solicitudes = new ArrayList<>();
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_SOLICITUD + "ORDER BY s.idSolicitud DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SolicitudServicio solicitud = mapearSolicitud(rs);
                solicitud.setDetalles(listarDetalles(conn, solicitud.getIdSolicitud()));
                solicitudes.add(solicitud);
            }
            return solicitudes;
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public SolicitudServicio buscarPorId(int idSolicitud) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_SOLICITUD + "WHERE s.idSolicitud = ?")) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                SolicitudServicio solicitud = mapearSolicitud(rs);
                solicitud.setDetalles(listarDetalles(conn, idSolicitud));
                return solicitud;
            }
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public String crear(SolicitudServicio solicitud, int idUsuario) {
        String sql = "INSERT INTO dbo.SolicitudServicio "
                + "(idCliente, fechaSolicitud, estado, observacion, idUsuarioRegistro) "
                + "VALUES (?, SYSDATETIME(), N'Pendiente', ?, ?)";
        try (Connection conn = ConexionBD.getInstancia().getConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, solicitud.getIdCliente());
                setNullable(ps, 2, solicitud.getObservacion());
                ps.setInt(3, idUsuario);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No se generó el identificador de la solicitud.");
                    insertarDetalles(conn, keys.getInt(1), solicitud.getDetalles());
                }
                conn.commit();
                return "OK";
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            registrarError(e);
            return "ERROR_BD";
        }
    }

    @Override
    public String editar(SolicitudServicio solicitud) {
        String sql = "UPDATE dbo.SolicitudServicio SET idCliente = ?, observacion = ? "
                + "WHERE idSolicitud = ? AND estado = N'Pendiente'";
        try (Connection conn = ConexionBD.getInstancia().getConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, solicitud.getIdCliente());
                setNullable(ps, 2, solicitud.getObservacion());
                ps.setInt(3, solicitud.getIdSolicitud());
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return "NO_EDITABLE";
                }
                try (PreparedStatement delete = conn.prepareStatement(
                        "DELETE FROM dbo.DetalleSolicitudServicio WHERE idSolicitud = ?")) {
                    delete.setInt(1, solicitud.getIdSolicitud());
                    delete.executeUpdate();
                }
                insertarDetalles(conn, solicitud.getIdSolicitud(), solicitud.getDetalles());
                conn.commit();
                return "OK";
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            registrarError(e);
            return "ERROR_BD";
        }
    }

    @Override
    public String aprobar(int idSolicitud) {
        String sql = "UPDATE dbo.SolicitudServicio SET estado = N'Aprobada' "
                + "WHERE idSolicitud = ? AND estado = N'Pendiente'";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            return ps.executeUpdate() == 1 ? "OK" : "NO_EDITABLE";
        } catch (SQLException e) {
            registrarError(e);
            return "ERROR_BD";
        }
    }

    @Override
    public String anular(int idSolicitud) {
        String sql = "UPDATE dbo.SolicitudServicio SET estado = N'Anulada' "
                + "WHERE idSolicitud = ? AND estado IN (N'Pendiente', N'Aprobada')";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            return ps.executeUpdate() == 1 ? "OK" : "NO_EDITABLE";
        } catch (SQLException e) {
            registrarError(e);
            return "ERROR_BD";
        }
    }

    @Override
    public int convertirEnVenta(int idSolicitud, int idUsuario) {
        String select = "SELECT idCliente, observacion, estado, idVenta FROM dbo.SolicitudServicio "
                + "WITH (UPDLOCK, ROWLOCK) WHERE idSolicitud = ?";
        String insertVenta = "INSERT INTO dbo.VentaServicio "
                + "(idCliente, fechaVenta, estado, observacion, idUsuarioRegistro) "
                + "VALUES (?, SYSDATETIME(), N'Pendiente', ?, ?)";
        try (Connection conn = ConexionBD.getInstancia().getConexion()) {
            conn.setAutoCommit(false);
            try {
                int idCliente;
                String observacion;
                try (PreparedStatement ps = conn.prepareStatement(select)) {
                    ps.setInt(1, idSolicitud);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getInt("idVenta") > 0
                                || !"Aprobada".equals(rs.getString("estado"))) {
                            conn.rollback();
                            return -1;
                        }
                        idCliente = rs.getInt("idCliente");
                        observacion = rs.getString("observacion");
                    }
                }

                int idVenta;
                try (PreparedStatement ps = conn.prepareStatement(insertVenta, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idCliente);
                    setNullable(ps, 2, observacion);
                    ps.setInt(3, idUsuario);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No se generó el identificador de la venta.");
                        idVenta = keys.getInt(1);
                    }
                }

                String copiarDetalles = "INSERT INTO dbo.DetalleVentaServicio "
                        + "(idVenta, idVehiculo, descripcionServicio, origen, destino, fechaServicio, cantidad, precioUnitario) "
                        + "SELECT ?, idVehiculo, descripcionServicio, origen, destino, fechaServicio, cantidad, precioEstimado "
                        + "FROM dbo.DetalleSolicitudServicio WHERE idSolicitud = ?";
                try (PreparedStatement ps = conn.prepareStatement(copiarDetalles)) {
                    ps.setInt(1, idVenta);
                    ps.setInt(2, idSolicitud);
                    if (ps.executeUpdate() == 0) throw new SQLException("La solicitud no tiene detalles.");
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE dbo.SolicitudServicio SET estado = N'Convertida', idVenta = ? WHERE idSolicitud = ?")) {
                    ps.setInt(1, idVenta);
                    ps.setInt(2, idSolicitud);
                    ps.executeUpdate();
                }
                conn.commit();
                return idVenta;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            registrarError(e);
            return -2;
        }
    }

    private void insertarDetalles(Connection conn, int idSolicitud, List<DetalleServicio> detalles)
            throws SQLException {
        String sql = "INSERT INTO dbo.DetalleSolicitudServicio "
                + "(idSolicitud, idVehiculo, descripcionServicio, origen, destino, fechaServicio, cantidad, precioEstimado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (DetalleServicio detalle : detalles) {
                ps.setInt(1, idSolicitud);
                ps.setInt(2, detalle.getIdVehiculo());
                ps.setString(3, detalle.getDescripcionServicio());
                setNullable(ps, 4, detalle.getOrigen());
                setNullable(ps, 5, detalle.getDestino());
                setTimestamp(ps, 6, detalle.getFechaServicio());
                ps.setBigDecimal(7, detalle.getCantidad());
                ps.setBigDecimal(8, detalle.getPrecioUnitario());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<DetalleServicio> listarDetalles(Connection conn, int idSolicitud) throws SQLException {
        List<DetalleServicio> detalles = new ArrayList<>();
        String sql = "SELECT d.idDetalleSolicitud, d.idVehiculo, v.placa, d.descripcionServicio, "
                + "d.origen, d.destino, d.fechaServicio, d.cantidad, d.precioEstimado "
                + "FROM dbo.DetalleSolicitudServicio d JOIN dbo.Vehiculo v ON v.idVehiculo = d.idVehiculo "
                + "WHERE d.idSolicitud = ? ORDER BY d.idDetalleSolicitud";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleServicio detalle = new DetalleServicio();
                    detalle.setIdDetalle(rs.getInt("idDetalleSolicitud"));
                    detalle.setIdVehiculo(rs.getInt("idVehiculo"));
                    detalle.setPlaca(rs.getString("placa"));
                    detalle.setDescripcionServicio(rs.getString("descripcionServicio"));
                    detalle.setOrigen(rs.getString("origen"));
                    detalle.setDestino(rs.getString("destino"));
                    detalle.setFechaServicio(fecha(rs.getTimestamp("fechaServicio")));
                    detalle.setCantidad(rs.getBigDecimal("cantidad"));
                    detalle.setPrecioUnitario(rs.getBigDecimal("precioEstimado"));
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    private SolicitudServicio mapearSolicitud(ResultSet rs) throws SQLException {
        SolicitudServicio solicitud = new SolicitudServicio();
        solicitud.setIdSolicitud(rs.getInt("idSolicitud"));
        solicitud.setIdCliente(rs.getInt("idCliente"));
        solicitud.setCliente(rs.getString("razonSocial"));
        solicitud.setFechaSolicitud(fecha(rs.getTimestamp("fechaSolicitud")));
        solicitud.setEstado(rs.getString("estado"));
        solicitud.setObservacion(rs.getString("observacion"));
        int venta = rs.getInt("idVenta");
        solicitud.setIdVenta(rs.wasNull() ? null : venta);
        return solicitud;
    }

    private LocalDateTime fecha(Timestamp value) { return value == null ? null : value.toLocalDateTime(); }

    private void setNullable(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) ps.setNull(index, Types.NVARCHAR);
        else ps.setString(index, value);
    }

    private void setTimestamp(PreparedStatement ps, int index, java.time.LocalDateTime value) throws SQLException {
        if (value == null) ps.setNull(index, Types.TIMESTAMP);
        else ps.setTimestamp(index, Timestamp.valueOf(value));
    }

    private void registrarError(SQLException e) {
        System.err.println("Error en SolicitudServicio (código " + e.getErrorCode() + ").");
    }
}
