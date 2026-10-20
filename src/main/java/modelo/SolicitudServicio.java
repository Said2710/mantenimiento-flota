package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SolicitudServicio {

    private int idSolicitud;
    private int idCliente;
    private String cliente;
    private LocalDateTime fechaSolicitud;
    private String estado;
    private String observacion;
    private Integer idVenta;
    private List<DetalleServicio> detalles = new ArrayList<>();

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer idVenta) { this.idVenta = idVenta; }
    public List<DetalleServicio> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleServicio> detalles) { this.detalles = detalles; }
}
