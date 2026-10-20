package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RegistroOperacion {

    private int idRegistro;
    private int idVehiculo;
    private String placa;
    private LocalDateTime fechaHora;
    private BigDecimal kilometraje;
    private BigDecimal horasMotor;
    private String observacion;
    private int idUsuarioRegistro;
    private String usuarioRegistro;

    public int getIdRegistro() { return idRegistro; }
    public void setIdRegistro(int idRegistro) { this.idRegistro = idRegistro; }
    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public BigDecimal getKilometraje() { return kilometraje; }
    public void setKilometraje(BigDecimal kilometraje) { this.kilometraje = kilometraje; }
    public BigDecimal getHorasMotor() { return horasMotor; }
    public void setHorasMotor(BigDecimal horasMotor) { this.horasMotor = horasMotor; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public int getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(int idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }
    public String getUsuarioRegistro() { return usuarioRegistro; }
    public void setUsuarioRegistro(String usuarioRegistro) { this.usuarioRegistro = usuarioRegistro; }
}
