package modelo;

import java.util.List;

public interface RepuestoRepository {

    String OK = "OK";
    String NO_ENCONTRADO = "NO_ENCONTRADO";
    String ERROR_BD = "ERROR_BD";

    List<Repuesto> listar();
    Repuesto buscarPorId(int idRepuesto);
    String crear(Repuesto repuesto);
    String editar(Repuesto repuesto);
    String eliminar(int idRepuesto);
}
