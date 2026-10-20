package modelo;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class VentaServicioRepositoryJdbc implements VentaServicioRepository {

    private static final String SELECT_VENTAS =
            "SELECT v.idVenta, v.idCliente, c.razonSocial, v.fechaVenta, v.estado, "
            + "v.observacion, v.idUsuarioRegistro FROM dbo.VentaServicio v "
            + "JOIN dbo.Cliente c ON c.idCliente = v.idCliente ";

    @Override
    public List<VentaServicio> listar() {
        List<VentaServicio> ventas = new ArrayList<>();
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_VENTAS + "ORDER BY v.idVenta DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                VentaServicio venta = mapear(rs);
                venta.setDetalles(listarDetalles(conn, venta.getIdVenta()));
                ventas.add(venta);
            }
            return ventas;
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public VentaServicio buscarPorId(int idVenta) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_VENTAS + "WHERE v.idVenta = ?")) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                VentaServicio venta = mapear(rs);
                venta.setDetalles(listarDetalles(conn, idVenta));
                return venta;
            }
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public String crear(VentaServicio venta) {
        String sql = "INSERT INTO dbo.VentaServicio "
                + "(idCliente, fechaVenta, estado, observacion, idUsuarioRegistro) "
                + "VALUES (?, SYSDATETIME(), N'Pendiente', ?, ?)";
        try (Connection conn = ConexionBD.getInstancia().getConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, venta.getIdCliente());
                setNullable(ps, 2, venta.getObservacion());
                ps.setInt(3, venta.getIdUsuarioRegistro());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No se generó el identificador de la venta.");
                    insertarDetalles(conn, keys.getInt(1), venta.getDetalles());
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
    public String editar(VentaServicio venta) {
        String sql = "UPDATE dbo.VentaServicio SET idCliente = ?, estado = ?, observacion = ? "
                + "WHERE idVenta = ? AND estado IN (N'Pendiente', N'Confirmada')";
        try (Connection conn = ConexionBD.getInstancia().getConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, venta.getIdCliente());
                ps.setString(2, venta.getEstado());
                setNullable(ps, 3, venta.getObservacion());
                ps.setInt(4, venta.getIdVenta());
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return "NO_EDITABLE";
                }
                try (PreparedStatement delete = conn.prepareStatement(
                        "DELETE FROM dbo.DetalleVentaServicio WHERE idVenta = ?")) {
                    delete.setInt(1, venta.getIdVenta());
                    delete.executeUpdate();
                }
                insertarDetalles(conn, venta.getIdVenta(), venta.getDetalles());
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
    public String anular(int idVenta) {
        String sql = "UPDATE dbo.VentaServicio SET estado = N'Anulada' "
                + "WHERE idVenta = ? AND estado <> N'Anulada'";
        try (Connection conn = ConexionBD.getInstancia().getConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idVenta);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return "NO_EDITABLE";
                }
                try (PreparedStatement docs = conn.prepareStatement(
                        "UPDATE dbo.DocumentoVenta SET estado = N'Anulado' "
                        + "WHERE idVenta = ? AND estado = N'Emitido'")) {
                    docs.setInt(1, idVenta);
                    docs.executeUpdate();
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

    private void insertarDetalles(Connection conn, int idVenta, List<DetalleServicio> detalles)
            throws SQLException {
        String sql = "INSERT INTO dbo.DetalleVentaServicio "
                + "(idVenta, idVehiculo, descripcionServicio, origen, destino, fechaServicio, cantidad, precioUnitario) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (DetalleServicio detalle : detalles) {
                ps.setInt(1, idVenta);
                ps.setInt(2, detalle.getIdVehiculo());
                ps.setString(3, detalle.getDescripcionServicio());
                setNullable(ps, 4, detalle.getOrigen());
                setNullable(ps, 5, detalle.getDestino());
                if (detalle.getFechaServicio() == null) ps.setNull(6, Types.TIMESTAMP);
                else ps.setTimestamp(6, Timestamp.valueOf(detalle.getFechaServicio()));
                ps.setBigDecimal(7, detalle.getCantidad());
                ps.setBigDecimal(8, detalle.getPrecioUnitario());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<DetalleServicio> listarDetalles(Connection conn, int idVenta) throws SQLException {
        List<DetalleServicio> detalles = new ArrayList<>();
        String sql = "SELECT d.idDetalle, d.idVehiculo, v.placa, d.descripcionServicio, d.origen, "
                + "d.destino, d.fechaServicio, d.cantidad, d.precioUnitario "
                + "FROM dbo.DetalleVentaServicio d LEFT JOIN dbo.Vehiculo v ON v.idVehiculo = d.idVehiculo "
                + "WHERE d.idVenta = ? ORDER BY d.idDetalle";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleServicio detalle = new DetalleServicio();
                    detalle.setIdDetalle(rs.getInt("idDetalle"));
                    int idVehiculo = rs.getInt("idVehiculo");
                    detalle.setIdVehiculo(rs.wasNull() ? null : idVehiculo);
                    detalle.setPlaca(rs.getString("placa"));
                    detalle.setDescripcionServicio(rs.getString("descripcionServicio"));
                    detalle.setOrigen(rs.getString("origen"));
                    detalle.setDestino(rs.getString("destino"));
                    Timestamp fecha = rs.getTimestamp("fechaServicio");
                    detalle.setFechaServicio(fecha == null ? null : fecha.toLocalDateTime());
                    detalle.setCantidad(rs.getBigDecimal("cantidad"));
                    detalle.setPrecioUnitario(rs.getBigDecimal("precioUnitario"));
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    private VentaServicio mapear(ResultSet rs) throws SQLException {
        VentaServicio venta = new VentaServicio();
        venta.setIdVenta(rs.getInt("idVenta"));
        venta.setIdCliente(rs.getInt("idCliente"));
        venta.setCliente(rs.getString("razonSocial"));
        Timestamp fecha = rs.getTimestamp("fechaVenta");
        venta.setFechaVenta(fecha == null ? null : fecha.toLocalDateTime());
        venta.setEstado(rs.getString("estado"));
        venta.setObservacion(rs.getString("observacion"));
        venta.setIdUsuarioRegistro(rs.getInt("idUsuarioRegistro"));
        return venta;
    }

    private void setNullable(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) ps.setNull(index, Types.NVARCHAR);
        else ps.setString(index, value);
    }

    private void registrarError(SQLException e) {
        System.err.println("Error en VentaServicio (código " + e.getErrorCode() + ").");
    }
}
