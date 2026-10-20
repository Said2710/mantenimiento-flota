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

public class VehiculoRepositoryJdbc implements VehiculoRepository {

    private static final String SELECT_BASE =
            "SELECT idVehiculo, placa, marca, modelo, anio, estado, fechaCreacion, "
            + "idUsuarioRegistro FROM dbo.Vehiculo ";

    @Override
    public List<Vehiculo> listar() {
        List<Vehiculo> vehiculos = new ArrayList<>();
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "ORDER BY placa");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                vehiculos.add(mapear(rs));
            }
            return vehiculos;
        } catch (SQLException e) {
            registrarError(e);
            return null;
        }
    }

    @Override
    public Vehiculo buscarPorId(int idVehiculo) {
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE idVehiculo = ?")) {
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
    public String crear(Vehiculo vehiculo) {
        String sql = "INSERT INTO dbo.Vehiculo "
                + "(placa, marca, modelo, anio, estado, fechaCreacion, idUsuarioRegistro) "
                + "VALUES (?, ?, ?, ?, ?, SYSDATETIME(), ?)";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            asignarCampos(ps, vehiculo);
            ps.setString(5, vehiculo.getEstado());
            if (vehiculo.getIdUsuarioRegistro() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, vehiculo.getIdUsuarioRegistro());
            }
            ps.executeUpdate();
            return OK;
        } catch (SQLException e) {
            return esDuplicado(e) ? DUPLICADO : errorBd(e);
        }
    }

    @Override
    public String editar(Vehiculo vehiculo) {
        String sql = "UPDATE dbo.Vehiculo SET placa = ?, marca = ?, modelo = ?, anio = ?, "
                + "estado = ? WHERE idVehiculo = ?";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            asignarCampos(ps, vehiculo);
            ps.setString(5, vehiculo.getEstado());
            ps.setInt(6, vehiculo.getIdVehiculo());
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return esDuplicado(e) ? DUPLICADO : errorBd(e);
        }
    }

    @Override
    public String eliminar(int idVehiculo) {
        String sql = "UPDATE dbo.Vehiculo SET estado = N'Inactivo' WHERE idVehiculo = ?";
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            return ps.executeUpdate() > 0 ? OK : NO_ENCONTRADO;
        } catch (SQLException e) {
            return errorBd(e);
        }
    }

    private void asignarCampos(PreparedStatement ps, Vehiculo vehiculo) throws SQLException {
        ps.setString(1, vehiculo.getPlaca());
        ps.setString(2, vehiculo.getMarca());
        ps.setString(3, vehiculo.getModelo());
        if (vehiculo.getAnio() == null) {
            ps.setNull(4, Types.SMALLINT);
        } else {
            ps.setShort(4, vehiculo.getAnio());
        }
    }

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        short anio = rs.getShort("anio");
        Short anioVehiculo = rs.wasNull() ? null : anio;
        int idUsuario = rs.getInt("idUsuarioRegistro");
        Integer usuarioRegistro = rs.wasNull() ? null : idUsuario;
        Timestamp fecha = rs.getTimestamp("fechaCreacion");
        return new Vehiculo(rs.getInt("idVehiculo"), rs.getString("placa"),
                rs.getString("marca"), rs.getString("modelo"), anioVehiculo,
                rs.getString("estado"), fecha == null ? null : fecha.toLocalDateTime(),
                usuarioRegistro);
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
