package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaServicio {

    private int idVenta;
    private int idCliente;
    private String cliente;
    private LocalDateTime fechaVenta;
    private String estado;
    private String observacion;
    private int idUsuarioRegistro;
    private List<DetalleServicio> detalles = new ArrayList<>();
    private List<DocumentoVenta> documentos = new ArrayList<>();

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public int getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(int idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }
    public List<DetalleServicio> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleServicio> detalles) { this.detalles = detalles; }
    public List<DocumentoVenta> getDocumentos() { return documentos; }
    public void setDocumentos(List<DocumentoVenta> documentos) { this.documentos = documentos; }
}
