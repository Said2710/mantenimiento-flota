package modelo;

import java.util.List;

public interface ClienteRepository {

    String OK = "OK";
    String DUPLICADO = "DUPLICADO";
    String NO_ENCONTRADO = "NO_ENCONTRADO";
    String ERROR_BD = "ERROR_BD";

    List<Cliente> listar();

    Cliente buscarPorId(int idCliente);

    String crear(Cliente cliente);

    String editar(Cliente cliente);

    String eliminar(int idCliente);
}
