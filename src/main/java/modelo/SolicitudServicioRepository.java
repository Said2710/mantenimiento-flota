package modelo;

import java.util.List;

public interface SolicitudServicioRepository {

    List<SolicitudServicio> listar();
    SolicitudServicio buscarPorId(int idSolicitud);
    String crear(SolicitudServicio solicitud, int idUsuario);
    String editar(SolicitudServicio solicitud);
    String aprobar(int idSolicitud);
    String anular(int idSolicitud);
    int convertirEnVenta(int idSolicitud, int idUsuario);
}
