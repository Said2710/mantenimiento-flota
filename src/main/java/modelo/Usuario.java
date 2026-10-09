package modelo;

import java.time.LocalDateTime;

/**
 * Entidad que representa a un usuario del sistema (tabla Usuarios).
 */
public class Usuario {

    private int idUsuario;
    private String nombreUsuario;
    private int rolId;
    private String rolNombre;
    private String estado;
    private boolean bloqueado;
    private int intentosFallidos;
    private LocalDateTime ultimoAcceso;

    public Usuario() {
    }

    public Usuario(int idUsuario, String nombreUsuario, int rolId, String rolNombre,
                   String estado, boolean bloqueado, int intentosFallidos,
                   LocalDateTime ultimoAcceso) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.rolId = rolId;
        this.rolNombre = rolNombre;
        this.estado = estado;
        this.bloqueado = bloqueado;
        this.intentosFallidos = intentosFallidos;
        this.ultimoAcceso = ultimoAcceso;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public int getRolId() { return rolId; }
    public void setRolId(int rolId) { this.rolId = rolId; }

    public String getRolNombre() { return rolNombre; }
    public void setRolNombre(String rolNombre) { this.rolNombre = rolNombre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    /** Indica si la cuenta está habilitada para iniciar sesión. */
    public boolean isActivo() {
        return "Activo".equals(estado);
    }

    /** Indica si el usuario tiene el rol de administrador. */
    public boolean esAdministrador() {
        return "Administrador".equals(rolNombre);
    }

    /** Dos primeras letras del nombre, en mayúscula (para el avatar). */
    public String getIniciales() {
        if (nombreUsuario == null || nombreUsuario.isEmpty()) {
            return "?";
        }
        return nombreUsuario.substring(0, Math.min(2, nombreUsuario.length())).toUpperCase();
    }
}