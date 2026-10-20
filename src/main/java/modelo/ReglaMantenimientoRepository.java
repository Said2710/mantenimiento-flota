package modelo;

import java.util.List;

public interface ReglaMantenimientoRepository {

    String OK = "OK";
    String NO_ENCONTRADO = "NO_ENCONTRADO";
    String ERROR_BD = "ERROR_BD";

    List<ReglaMantenimiento> listar();

    ReglaMantenimiento buscarPorId(int idRegla);

    String crear(ReglaMantenimiento regla);

    String editar(ReglaMantenimiento regla);

    String desactivar(int idRegla);
}
