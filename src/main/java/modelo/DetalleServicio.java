package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DetalleServicio {

    private int idDetalle;
    private Integer idVehiculo;
    private String placa;
    private String descripcionServicio;
    private String origen;
    private String destino;
    private LocalDateTime fechaServicio;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;

    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }
    public Integer getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(Integer idVehiculo) { this.idVehiculo = idVehiculo; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getDescripcionServicio() { return descripcionServicio; }
    public void setDescripcionServicio(String descripcionServicio) { this.descripcionServicio = descripcionServicio; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public LocalDateTime getFechaServicio() { return fechaServicio; }
    public void setFechaServicio(LocalDateTime fechaServicio) { this.fechaServicio = fechaServicio; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}
