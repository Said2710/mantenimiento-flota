package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReglaMantenimiento {

    private int idRegla;
    private int idComponente;
    private String nombreComponente;
    private String nombre;
    private String tipoMetrica;
    private BigDecimal intervalo;
    private String descripcion;
    private boolean activo;
    private LocalDateTime fechaCreacion;

    public ReglaMantenimiento() {
    }

    public ReglaMantenimiento(int idRegla, int idComponente, String nombreComponente,
                              String nombre, String tipoMetrica, BigDecimal intervalo,
                              String descripcion, boolean activo, LocalDateTime fechaCreacion) {
        this.idRegla = idRegla;
        this.idComponente = idComponente;
        this.nombreComponente = nombreComponente;
        this.nombre = nombre;
        this.tipoMetrica = tipoMetrica;
        this.intervalo = intervalo;
        this.descripcion = descripcion;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdRegla() { return idRegla; }
    public void setIdRegla(int idRegla) { this.idRegla = idRegla; }
    public int getIdComponente() { return idComponente; }
    public void setIdComponente(int idComponente) { this.idComponente = idComponente; }
    public String getNombreComponente() { return nombreComponente; }
    public void setNombreComponente(String nombreComponente) { this.nombreComponente = nombreComponente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipoMetrica() { return tipoMetrica; }
    public void setTipoMetrica(String tipoMetrica) { this.tipoMetrica = tipoMetrica; }
    public BigDecimal getIntervalo() { return intervalo; }
    public void setIntervalo(BigDecimal intervalo) { this.intervalo = intervalo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
