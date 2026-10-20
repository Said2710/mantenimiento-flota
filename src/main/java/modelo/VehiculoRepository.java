package modelo;

import java.util.List;

public interface VehiculoRepository {

    String OK = "OK";
    String DUPLICADO = "DUPLICADO";
    String NO_ENCONTRADO = "NO_ENCONTRADO";
    String ERROR_BD = "ERROR_BD";

    List<Vehiculo> listar();

    Vehiculo buscarPorId(int idVehiculo);

    String crear(Vehiculo vehiculo);

    String editar(Vehiculo vehiculo);

    String eliminar(int idVehiculo);
}
