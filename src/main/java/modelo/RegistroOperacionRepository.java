package modelo;

import java.util.List;

public interface RegistroOperacionRepository {

    String OK = "OK";
    String NO_ENCONTRADO = "NO_ENCONTRADO";
    String ERROR_BD = "ERROR_BD";

    List<RegistroOperacion> listar();

    RegistroOperacion ultimoPorVehiculo(int idVehiculo);

    String crear(RegistroOperacion registro);
}
