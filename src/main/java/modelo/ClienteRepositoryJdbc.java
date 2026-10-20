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

public class ClienteRepositoryJdbc implements ClienteRepository {

    private static final String SELECT_BASE =
            "SELECT idCliente, razonSocial, numeroDocumento, correo, telefono, direccion, "
            + "estado, fechaCreacion, idUsuarioRegistro FROM dbo.Cliente ";

    @Override
    public List<Cliente> listar() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY razonSocial";

        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
            return clientes;
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public Cliente buscarPorId(int idCliente) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE idCliente = ?")) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public String crear(Cliente cliente) {
        String sql = "INSERT INTO dbo.Cliente "
                + "(razonSocial, numeroDocumento, correo, telefono, direccion, estado, "
                + "fechaCreacion, idUsuarioRegistro) "
                + "VALUES (?, ?, ?, ?, ?, ?, SYSDATETIME(), ?)";

        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getRazonSocial());
            setNullable(ps, 2, cliente.getNumeroDocumento());
            setNullable(ps, 3, cliente.getCorreo());
            setNullable(ps, 4, cliente.getTelefono());
            setNullable(ps, 5, cliente.getDireccion());
            ps.setString(6, cliente.getEstado());
            if (cliente.getIdUsuarioRegistro() == null) {
                ps.setNull(7, Types.INTEGER);
            } else {
                ps.setInt(7, cliente.getIdUsuarioRegistro());
            }
            ps.executeUpdate();
            return OK;
        } catch (SQLException e) {
            return esDuplicado(e) ? DUPLICADO : errorBd(e);
        }
    }

    @Override
    public String editar(Cliente cliente) {
        String sql = "UPDATE dbo.Cliente SET razonSocial = ?, numeroDocumento = ?, correo = ?, "
                + "telefono = ?, direccion = ?, estado = ? WHERE idCliente = ?";

        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getRazonSocial());
            setNullable(ps, 2, cliente.getNumeroDocumento());
            setNullable(ps, 3, cliente.getCorreo());
            setNullable(ps, 4, cliente.getTelefono());
            setNullable(ps, 5, cliente.getDireccion());
            ps.setString(6, cliente.getEstado());
            ps.setInt(7, cliente.getIdCliente());
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return esDuplicado(e) ? DUPLICADO : errorBd(e);
        }
    }

    @Override
    public String eliminar(int idCliente) {
        String sql = "UPDATE dbo.Cliente SET estado = 'Inactivo' WHERE idCliente = ?";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Timestamp fecha = rs.getTimestamp("fechaCreacion");
        int idUsuario = rs.getInt("idUsuarioRegistro");
        Integer idUsuarioRegistro = rs.wasNull() ? null : idUsuario;

        return new Cliente(
                rs.getInt("idCliente"),
                rs.getString("razonSocial"),
                rs.getString("numeroDocumento"),
                rs.getString("correo"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                rs.getString("estado"),
                fecha == null ? null : fecha.toLocalDateTime(),
                idUsuarioRegistro);
    }

    private void setNullable(PreparedStatement ps, int indice, String valor) throws SQLException {
        if (valor == null || valor.isBlank()) {
            ps.setNull(indice, Types.VARCHAR);
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
