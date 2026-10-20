package modelo;

import conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProveedorRepositoryJdbc implements ProveedorRepository {

    private static final String SELECT_BASE =
            "SELECT idProveedor, razonSocial, tipoProveedor, numeroDocumento, contacto, correo, "
            + "telefono, direccion, estado, idUsuarioRegistro FROM dbo.Proveedor ";

    @Override
    public List<Proveedor> listar() {
        List<Proveedor> proveedores = new ArrayList<>();
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY razonSocial");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) proveedores.add(mapear(rs));
            return proveedores;
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public Proveedor buscarPorId(int idProveedor) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE idProveedor = ?")) {
            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public String crear(Proveedor proveedor) {
        String sql = "INSERT INTO dbo.Proveedor (razonSocial, tipoProveedor, numeroDocumento, contacto, "
                + "correo, telefono, direccion, estado, fechaCreacion, idUsuarioRegistro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), ?)";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, proveedor.getRazonSocial());
            ps.setString(2, proveedor.getTipoProveedor());
            setNullable(ps, 3, proveedor.getNumeroDocumento());
            setNullable(ps, 4, proveedor.getContacto());
            setNullable(ps, 5, proveedor.getCorreo());
            setNullable(ps, 6, proveedor.getTelefono());
            setNullable(ps, 7, proveedor.getDireccion());
            ps.setString(8, proveedor.getEstado());
            setNullable(ps, 9, proveedor.getIdUsuarioRegistro());
            ps.executeUpdate();
            return OK;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    @Override
    public String editar(Proveedor proveedor) {
        String sql = "UPDATE dbo.Proveedor SET razonSocial = ?, tipoProveedor = ?, numeroDocumento = ?, "
                + "contacto = ?, correo = ?, telefono = ?, direccion = ?, estado = ? WHERE idProveedor = ?";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, proveedor.getRazonSocial());
            ps.setString(2, proveedor.getTipoProveedor());
            setNullable(ps, 3, proveedor.getNumeroDocumento());
            setNullable(ps, 4, proveedor.getContacto());
            setNullable(ps, 5, proveedor.getCorreo());
            setNullable(ps, 6, proveedor.getTelefono());
            setNullable(ps, 7, proveedor.getDireccion());
            ps.setString(8, proveedor.getEstado());
            ps.setInt(9, proveedor.getIdProveedor());
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    @Override
    public String eliminar(int idProveedor) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE dbo.Proveedor SET estado = N'Inactivo' WHERE idProveedor = ?")) {
            ps.setInt(1, idProveedor);
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(rs.getInt("idProveedor"));
        proveedor.setRazonSocial(rs.getString("razonSocial"));
        proveedor.setTipoProveedor(rs.getString("tipoProveedor"));
        proveedor.setNumeroDocumento(rs.getString("numeroDocumento"));
        proveedor.setContacto(rs.getString("contacto"));
        proveedor.setCorreo(rs.getString("correo"));
        proveedor.setTelefono(rs.getString("telefono"));
        proveedor.setDireccion(rs.getString("direccion"));
        proveedor.setEstado(rs.getString("estado"));
        int idUsuario = rs.getInt("idUsuarioRegistro");
        proveedor.setIdUsuarioRegistro(rs.wasNull() ? null : idUsuario);
        return proveedor;
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
        System.err.println("Error en Proveedor (código " + e.getErrorCode() + ").");
        return ERROR_BD;
    }

    private void registrarError(SQLException e) {
        System.err.println("Error en Proveedor (código " + e.getErrorCode() + ").");
    }
}
