package modelo;

import java.util.List;

public interface DocumentoVentaRepository {

    List<DocumentoVenta> listarPorVenta(int idVenta);
    String crear(DocumentoVenta documento, int idUsuario);
    String anular(int idDocumento);
}
