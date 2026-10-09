package modelo;

import java.util.List;

/**
 * Patrón Repository: define cómo se accede a los usuarios sin que el controlador
 * conozca la base de datos que hay detrás.
 */
public interface UsuarioRepository {

    // Resultados posibles de las operaciones de escritura
    String OK = "OK";
    String DUPLICADO = "DUPLICADO";
    String NO_ENCONTRADO = "NO_ENCONTRADO";
    String ERROR_BD = "ERROR_BD";

    List<Usuario> listar();

    Usuario buscarPorId(int idUsuario);

    Usuario buscarPorNombre(String nombreUsuario);

    String crear(String nombreUsuario, String contrasena, int rolId, String estado, String registradoPor);

    String editar(int idUsuario, String nombreUsuario, String contrasenaNueva, int rolId, String estado);

    String eliminar(int idUsuario);

    String desbloquear(int idUsuario);
}