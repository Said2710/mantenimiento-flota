package modelo;

public class Componente {

    private int idComponente;
    private String nombre;
    private String descripcion;
    private String estado;

    public Componente() {
    }

    public Componente(int idComponente, String nombre, String descripcion, String estado) {
        this.idComponente = idComponente;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public int getIdComponente() { return idComponente; }
    public void setIdComponente(int idComponente) { this.idComponente = idComponente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
