package modelo;

import conexion.ConexionBD;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class EvaluacionMantenimientoRepositoryJdbc implements EvaluacionMantenimientoRepository {

    private static final BigDecimal PORCENTAJE_PROXIMO = new BigDecimal("0.10");

    private static final String SQL =
            "SELECT v.idVehiculo, v.placa, r.idRegla, r.nombre AS nombreRegla, "
            + "c.nombre AS nombreComponente, r.tipoMetrica, r.intervalo, "
            + "lectura.kilometraje, lectura.horasMotor, servicio.idOrden, servicio.fechaCierre, "
            + "servicio.kilometrajeServicio, servicio.horasMotorServicio "
            + "FROM dbo.Vehiculo v "
            + "CROSS JOIN dbo.ReglaMantenimiento r "
            + "INNER JOIN dbo.Componente c ON c.idComponente = r.idComponente "
            + "OUTER APPLY (SELECT TOP (1) ro.kilometraje, ro.horasMotor "
            + "FROM dbo.RegistroOperacion ro WHERE ro.idVehiculo = v.idVehiculo "
            + "ORDER BY ro.fechaHora DESC, ro.idRegistro DESC) lectura "
            + "OUTER APPLY (SELECT TOP (1) om.idOrden, om.fechaCierre, om.kilometrajeServicio, "
            + "om.horasMotorServicio FROM dbo.OrdenMantenimiento om "
            + "WHERE om.idVehiculo = v.idVehiculo AND om.idRegla = r.idRegla "
            + "AND om.estado = N'Completada' AND om.fechaCierre IS NOT NULL "
            + "ORDER BY om.fechaCierre DESC, om.idOrden DESC) servicio "
            + "WHERE v.estado = N'Operativo' AND r.activo = 1 AND c.estado = N'Activo' "
            + "ORDER BY v.placa, c.nombre, r.nombre";

    @Override
    public List<EvaluacionMantenimiento> evaluar() {
        List<EvaluacionMantenimiento> evaluaciones = new ArrayList<>();
        try (Connection conn = ConexionBD.getInstancia().getConexion();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) evaluaciones.add(mapear(rs));
            return evaluaciones;
        } catch (SQLException e) {
            System.err.println("Error al evaluar mantenimiento (código " + e.getErrorCode() + ").");
            return null;
        }
    }

    private EvaluacionMantenimiento mapear(ResultSet rs) throws SQLException {
        EvaluacionMantenimiento evaluacion = new EvaluacionMantenimiento();
        evaluacion.setIdVehiculo(rs.getInt("idVehiculo"));
        evaluacion.setPlaca(rs.getString("placa"));
        evaluacion.setIdRegla(rs.getInt("idRegla"));
        evaluacion.setNombreRegla(rs.getNString("nombreRegla"));
        evaluacion.setNombreComponente(rs.getNString("nombreComponente"));
        evaluacion.setTipoMetrica(rs.getNString("tipoMetrica"));
        evaluacion.setIntervalo(rs.getBigDecimal("intervalo"));

        BigDecimal actual;
        BigDecimal base;
        BigDecimal restante;
        Timestamp fechaServicio = rs.getTimestamp("fechaCierre");
        evaluacion.setFechaUltimoServicio(fechaServicio == null ? null : fechaServicio.toLocalDateTime());
        int idOrden = rs.getInt("idOrden");
        evaluacion.setIdOrdenReferencia(rs.wasNull() ? null : idOrden);

        switch (evaluacion.getTipoMetrica()) {
            case "KILOMETRAJE" -> {
                actual = rs.getBigDecimal("kilometraje");
                base = rs.getBigDecimal("kilometrajeServicio");
                if (evaluacion.getIdOrdenReferencia() == null) return sinDatos(evaluacion, "SIN_HISTORIAL");
                if (actual == null) return sinDatos(evaluacion, "SIN_LECTURA");
                if (base == null) return sinDatos(evaluacion, "SIN_BASE");
                if (actual.compareTo(base) < 0) return sinDatos(evaluacion, "LECTURA_INCONSISTENTE");
                restante = evaluacion.getIntervalo().subtract(actual.subtract(base));
                evaluacion.setValorActual(actual);
                evaluacion.setValorUltimoServicio(base);
                return clasificar(evaluacion, restante);
            }
            case "HORAS_MOTOR" -> {
                actual = rs.getBigDecimal("horasMotor");
                base = rs.getBigDecimal("horasMotorServicio");
                if (evaluacion.getIdOrdenReferencia() == null) return sinDatos(evaluacion, "SIN_HISTORIAL");
                if (actual == null) return sinDatos(evaluacion, "SIN_LECTURA");
                if (base == null) return sinDatos(evaluacion, "SIN_BASE");
                if (actual.compareTo(base) < 0) return sinDatos(evaluacion, "LECTURA_INCONSISTENTE");
                restante = evaluacion.getIntervalo().subtract(actual.subtract(base));
                evaluacion.setValorActual(actual);
                evaluacion.setValorUltimoServicio(base);
                return clasificar(evaluacion, restante);
            }
            case "DIAS" -> {
                if (evaluacion.getIdOrdenReferencia() == null) return sinDatos(evaluacion, "SIN_HISTORIAL");
                if (fechaServicio == null) return sinDatos(evaluacion, "SIN_BASE");
                actual = BigDecimal.valueOf(ChronoUnit.DAYS.between(
                        fechaServicio.toLocalDateTime().toLocalDate(),
                        LocalDate.now(ZoneId.of("America/Lima"))));
                if (actual.signum() < 0) return sinDatos(evaluacion, "LECTURA_INCONSISTENTE");
                restante = evaluacion.getIntervalo().subtract(actual);
                evaluacion.setValorActual(actual);
                evaluacion.setValorUltimoServicio(BigDecimal.ZERO);
                return clasificar(evaluacion, restante);
            }
            default -> { return sinDatos(evaluacion, "SIN_DATOS"); }
        }
    }

    private EvaluacionMantenimiento clasificar(EvaluacionMantenimiento evaluacion, BigDecimal restante) {
        BigDecimal valorRestante = restante.setScale(2, RoundingMode.HALF_UP);
        evaluacion.setValorRestante(valorRestante);
        if (restante.signum() <= 0) {
            evaluacion.setEstado("VENCIDO");
        } else if (restante.compareTo(evaluacion.getIntervalo().multiply(PORCENTAJE_PROXIMO)) <= 0) {
            evaluacion.setEstado("PROXIMO");
        } else {
            evaluacion.setEstado("AL_DIA");
        }
        return evaluacion;
    }

    private EvaluacionMantenimiento sinDatos(EvaluacionMantenimiento evaluacion, String estado) {
        evaluacion.setEstado(estado);
        return evaluacion;
    }
}
