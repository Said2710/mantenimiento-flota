package modelo;

import java.util.List;

public interface ProveedorRepository {

    String OK = "OK";
    String NO_ENCONTRADO = "NO_ENCONTRADO";
    String ERROR_BD = "ERROR_BD";

    List<Proveedor> listar();
    Proveedor buscarPorId(int idProveedor);
    String crear(Proveedor proveedor);
    String editar(Proveedor proveedor);
    String eliminar(int idProveedor);
}
