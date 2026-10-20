package modelo;

import java.time.LocalDateTime;

public class Vehiculo {

    private int idVehiculo;
    private String placa;
    private String marca;
    private String modelo;
    private Short anio;
    private String estado;
    private LocalDateTime fechaCreacion;
    private Integer idUsuarioRegistro;

    public Vehiculo() {
    }

    public Vehiculo(int idVehiculo, String placa, String marca, String modelo, Short anio,
                    String estado, LocalDateTime fechaCreacion, Integer idUsuarioRegistro) {
        this.idVehiculo = idVehiculo;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.idUsuarioRegistro = idUsuarioRegistro;
    }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public Short getAnio() { return anio; }
    public void setAnio(Short anio) { this.anio = anio; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Integer getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(Integer idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }
}
