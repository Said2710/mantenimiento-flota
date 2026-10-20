package modelo;

import java.util.List;

public interface VentaServicioRepository {

    List<VentaServicio> listar();
    VentaServicio buscarPorId(int idVenta);
    String crear(VentaServicio venta);
    String editar(VentaServicio venta);
    String anular(int idVenta);
}
