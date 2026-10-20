package modelo;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DocumentoVentaRepositoryJdbc implements DocumentoVentaRepository {

    @Override
    public List<DocumentoVenta> listarPorVenta(int idVenta) {
        List<DocumentoVenta> documentos = new ArrayList<>();
        String sql = "SELECT idDocumento, idVenta, tipoDocumento, serie, numero, fechaEmision, estado, observacion "
                + "FROM dbo.DocumentoVenta WHERE idVenta = ? ORDER BY fechaEmision DESC";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DocumentoVenta documento = new DocumentoVenta();
                    documento.setIdDocumento(rs.getInt("idDocumento"));
                    documento.setIdVenta(rs.getInt("idVenta"));
                    documento.setTipoDocumento(rs.getString("tipoDocumento"));
                    documento.setSerie(rs.getString("serie"));
                    documento.setNumero(rs.getString("numero"));
                    Timestamp fecha = rs.getTimestamp("fechaEmision");
                    documento.setFechaEmision(fecha == null ? null : fecha.toLocalDateTime());
                    documento.setEstado(rs.getString("estado"));
                    documento.setObservacion(rs.getString("observacion"));
                    documentos.add(documento);
                }
            }
            return documentos;
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public String crear(DocumentoVenta documento, int idUsuario) {
        String sql = "INSERT INTO dbo.DocumentoVenta "
                + "(idVenta, tipoDocumento, serie, numero, fechaEmision, estado, observacion, idUsuarioRegistro) "
                + "SELECT ?, ?, ?, ?, SYSDATETIME(), N'Emitido', ?, ? "
                + "WHERE EXISTS (SELECT 1 FROM dbo.VentaServicio WHERE idVenta = ? AND estado IN (N'Confirmada', N'Facturada')) "
                + "AND NOT (? IN (N'Factura', N'Boleta') AND EXISTS ("
                + "SELECT 1 FROM dbo.DocumentoVenta WHERE idVenta = ? AND tipoDocumento IN (N'Factura', N'Boleta') "
                + "AND estado = N'Emitido'))";
        try (Connection conn = ConexionBD.getInstancia().getConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, documento.getIdVenta());
                ps.setString(2, documento.getTipoDocumento());
                ps.setString(3, documento.getSerie());
                ps.setString(4, documento.getNumero());
                if (documento.getObservacion() == null || documento.getObservacion().isBlank()) {
                    ps.setNull(5, Types.NVARCHAR);
                } else {
                    ps.setString(5, documento.getObservacion());
                }
                ps.setInt(6, idUsuario);
                ps.setInt(7, documento.getIdVenta());
                ps.setString(8, documento.getTipoDocumento());
                ps.setInt(9, documento.getIdVenta());
                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return "NO_EDITABLE";
                }
                if (!"Guia".equals(documento.getTipoDocumento())) {
                    try (PreparedStatement venta = conn.prepareStatement(
                            "UPDATE dbo.VentaServicio SET estado = N'Facturada' WHERE idVenta = ?")) {
                        venta.setInt(1, documento.getIdVenta());
                        venta.executeUpdate();
                    }
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
    public String anular(int idDocumento) {
        String sql = "UPDATE dbo.DocumentoVenta SET estado = N'Anulado' "
                + "WHERE idDocumento = ? AND estado = N'Emitido'";
        try (Connection conn = ConexionBD.getInstancia().getConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idDocumento);
                if (ps.executeUpdate() != 1) {
                    conn.rollback();
                    return "NO_EDITABLE";
                }
                String actualizarVenta = "UPDATE dbo.VentaServicio SET estado = N'Confirmada' "
                        + "WHERE idVenta = (SELECT idVenta FROM dbo.DocumentoVenta WHERE idDocumento = ?) "
                        + "AND estado = N'Facturada' AND NOT EXISTS ("
                        + "SELECT 1 FROM dbo.DocumentoVenta WHERE idVenta = "
                        + "(SELECT idVenta FROM dbo.DocumentoVenta WHERE idDocumento = ?) "
                        + "AND tipoDocumento IN (N'Factura', N'Boleta') AND estado = N'Emitido')";
                try (PreparedStatement venta = conn.prepareStatement(actualizarVenta)) {
                    venta.setInt(1, idDocumento);
                    venta.setInt(2, idDocumento);
                    venta.executeUpdate();
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

    private void registrarError(SQLException e) {
        System.err.println("Error en DocumentoVenta (código " + e.getErrorCode() + ").");
    }
}
