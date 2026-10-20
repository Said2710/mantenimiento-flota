package modelo;

import java.util.List;

public interface ComponenteRepository {

    String OK = "OK";
    String DUPLICADO = "DUPLICADO";
    String NO_ENCONTRADO = "NO_ENCONTRADO";
    String ERROR_BD = "ERROR_BD";

    List<Componente> listar();

    Componente buscarPorId(int idComponente);

    String crear(Componente componente);

    String editar(Componente componente);

    String eliminar(int idComponente);
}
