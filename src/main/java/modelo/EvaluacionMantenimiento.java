package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EvaluacionMantenimiento {

    private int idVehiculo;
    private String placa;
    private int idRegla;
    private String nombreRegla;
    private String nombreComponente;
    private String tipoMetrica;
    private BigDecimal intervalo;
    private BigDecimal valorActual;
    private BigDecimal valorUltimoServicio;
    private Integer idOrdenReferencia;
    private LocalDateTime fechaUltimoServicio;
    private BigDecimal valorRestante;
    private String estado;

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public int getIdRegla() { return idRegla; }
    public void setIdRegla(int idRegla) { this.idRegla = idRegla; }
    public String getNombreRegla() { return nombreRegla; }
    public void setNombreRegla(String nombreRegla) { this.nombreRegla = nombreRegla; }
    public String getNombreComponente() { return nombreComponente; }
    public void setNombreComponente(String nombreComponente) { this.nombreComponente = nombreComponente; }
    public String getTipoMetrica() { return tipoMetrica; }
    public void setTipoMetrica(String tipoMetrica) { this.tipoMetrica = tipoMetrica; }
    public BigDecimal getIntervalo() { return intervalo; }
    public void setIntervalo(BigDecimal intervalo) { this.intervalo = intervalo; }
    public BigDecimal getValorActual() { return valorActual; }
    public void setValorActual(BigDecimal valorActual) { this.valorActual = valorActual; }
    public BigDecimal getValorUltimoServicio() { return valorUltimoServicio; }
    public void setValorUltimoServicio(BigDecimal valorUltimoServicio) { this.valorUltimoServicio = valorUltimoServicio; }
    public Integer getIdOrdenReferencia() { return idOrdenReferencia; }
    public void setIdOrdenReferencia(Integer idOrdenReferencia) { this.idOrdenReferencia = idOrdenReferencia; }
    public LocalDateTime getFechaUltimoServicio() { return fechaUltimoServicio; }
    public void setFechaUltimoServicio(LocalDateTime fechaUltimoServicio) { this.fechaUltimoServicio = fechaUltimoServicio; }
    public BigDecimal getValorRestante() { return valorRestante; }
    public void setValorRestante(BigDecimal valorRestante) { this.valorRestante = valorRestante; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
