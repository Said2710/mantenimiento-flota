package modelo;

import java.time.LocalDateTime;

public class Cliente {

    private int idCliente;
    private String razonSocial;
    private String numeroDocumento;
    private String correo;
    private String telefono;
    private String direccion;
    private String estado;
    private LocalDateTime fechaCreacion;
    private Integer idUsuarioRegistro;

    public Cliente() {
    }

    public Cliente(int idCliente, String razonSocial, String numeroDocumento, String correo,
                   String telefono, String direccion, String estado,
                   LocalDateTime fechaCreacion, Integer idUsuarioRegistro) {
        this.idCliente = idCliente;
        this.razonSocial = razonSocial;
        this.numeroDocumento = numeroDocumento;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.idUsuarioRegistro = idUsuarioRegistro;
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Integer getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(Integer idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }
}
