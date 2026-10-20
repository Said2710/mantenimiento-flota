package controlador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import modelo.Usuario;
import modelo.UsuarioRepository;
import modelo.UsuarioRepositoryJdbc;
import modelo.Vehiculo;
import modelo.VehiculoRepository;
import modelo.VehiculoRepositoryJdbc;

@WebServlet(name = "VehiculoServlet", urlPatterns = {"/VehiculoServlet"})
public class VehiculoServlet extends HttpServlet {

    private final VehiculoRepository vehiculos = new VehiculoRepositoryJdbc();
    private final UsuarioRepository usuarios = new UsuarioRepositoryJdbc();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario actual = validarAdministrador(request, response, session);
        if (actual == null) return;

        List<Vehiculo> lista = vehiculos.listar();
        if (lista == null) {
            lista = new ArrayList<>();
            request.setAttribute("mensaje", "No se pudo cargar la lista de vehículos.");
            request.setAttribute("tipoMensaje", "danger");
        } else {
            recuperarMensaje(request, session);
        }

        String idEditar = request.getParameter("editar");
        if (idEditar != null) {
            try {
                Vehiculo vehiculo = vehiculos.buscarPorId(Integer.parseInt(idEditar));
                if (vehiculo != null) request.setAttribute("vehiculoEditar", vehiculo);
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador del vehículo no es válido.");
                recuperarMensaje(request, session);
            }
        }

        request.setAttribute("vehiculos", lista);
        request.getRequestDispatcher("vehiculos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Usuario actual = validarAdministrador(request, response, session);
        if (actual == null) return;

        String accion = request.getParameter("accion");
        if ("crear".equals(accion)) {
            guardar(request, session, actual, false);
        } else if ("editar".equals(accion)) {
            guardar(request, session, actual, true);
        } else if ("eliminar".equals(accion)) {
            inactivar(request, session);
        } else {
            mensaje(session, "danger", "La acción solicitada no es válida.");
        }
        response.sendRedirect("VehiculoServlet");
    }

    private void guardar(HttpServletRequest request, HttpSession session,
                         Usuario actual, boolean editar) {
        Vehiculo vehiculo = leerVehiculo(request);
        String error = validar(vehiculo, request.getParameter("anio"));
        if (error != null) {
            mensaje(session, "danger", error);
            return;
        }

        String resultado;
        if (editar) {
            try {
                vehiculo.setIdVehiculo(Integer.parseInt(request.getParameter("idVehiculo")));
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador del vehículo no es válido.");
                return;
            }
            resultado = vehiculos.editar(vehiculo);
        } else {
            vehiculo.setIdUsuarioRegistro(actual.getIdUsuario());
            resultado = vehiculos.crear(vehiculo);
        }

        if (VehiculoRepository.OK.equals(resultado)) {
            mensaje(session, "success", editar ? "Vehículo actualizado." : "Vehículo registrado.");
        } else if (VehiculoRepository.DUPLICADO.equals(resultado)) {
            mensaje(session, "warning", "La placa ya está registrada.");
        } else if (VehiculoRepository.NO_ENCONTRADO.equals(resultado)) {
            mensaje(session, "warning", "No se encontró el vehículo.");
        } else {
            mensaje(session, "danger", "No se pudo guardar el vehículo. Revisa la conexión con la base de datos.");
        }
    }

    private void inactivar(HttpServletRequest request, HttpSession session) {
        try {
            String resultado = vehiculos.eliminar(Integer.parseInt(request.getParameter("idVehiculo")));
            if (VehiculoRepository.OK.equals(resultado)) {
                mensaje(session, "success", "Vehículo inactivado.");
            } else if (VehiculoRepository.NO_ENCONTRADO.equals(resultado)) {
                mensaje(session, "warning", "No se encontró el vehículo.");
            } else {
                mensaje(session, "danger", "No se pudo inactivar el vehículo.");
            }
        } catch (NumberFormatException e) {
            mensaje(session, "danger", "El identificador del vehículo no es válido.");
        }
    }

    private Vehiculo leerVehiculo(HttpServletRequest request) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(limpiar(request.getParameter("placa")).toUpperCase());
        vehiculo.setMarca(limpiar(request.getParameter("marca")));
        vehiculo.setModelo(limpiar(request.getParameter("modelo")));
        vehiculo.setEstado(limpiar(request.getParameter("estado")));
        String anio = limpiar(request.getParameter("anio"));
        if (!anio.isEmpty()) {
            try {
                vehiculo.setAnio(Short.valueOf(anio));
            } catch (NumberFormatException e) {
                vehiculo.setAnio(null);
            }
        }
        return vehiculo;
    }

    private String validar(Vehiculo vehiculo, String anioTexto) {
        if (vehiculo.getPlaca().isBlank() || vehiculo.getMarca().isBlank()
                || vehiculo.getModelo().isBlank()) {
            return "Placa, marca y modelo son obligatorios.";
        }
        if (vehiculo.getPlaca().length() > 12 || vehiculo.getMarca().length() > 60
                || vehiculo.getModelo().length() > 80) {
            return "Uno de los campos supera el máximo permitido por la base de datos.";
        }
        if (!limpiar(anioTexto).isEmpty() && vehiculo.getAnio() == null) {
            return "Ingresa un año válido.";
        }
        if (vehiculo.getAnio() != null && (vehiculo.getAnio() < 1950 || vehiculo.getAnio() > 2100)) {
            return "El año debe estar entre 1950 y 2100.";
        }
        if (!"Operativo".equals(vehiculo.getEstado())
                && !"En mantenimiento".equals(vehiculo.getEstado())
                && !"Inactivo".equals(vehiculo.getEstado())) {
            return "Selecciona un estado válido.";
        }
        return null;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private Usuario validarAdministrador(HttpServletRequest request,
                                         HttpServletResponse response,
                                         HttpSession session)
            throws ServletException, IOException {
        String nombre = session == null ? null : (String) session.getAttribute("usuarioLogueado");
        if (nombre == null) {
            response.sendRedirect("login.jsp");
            return null;
        }
        Usuario actual = usuarios.buscarPorNombre(nombre);
        if (actual == null || !actual.esAdministrador()) {
            session.invalidate();
            request.setAttribute("error", "Tu cuenta no tiene permiso para gestionar vehículos.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return null;
        }
        return actual;
    }

    private void mensaje(HttpSession session, String tipo, String texto) {
        session.setAttribute("vehiculoFlashTipo", tipo);
        session.setAttribute("vehiculoFlashMensaje", texto);
    }

    private void recuperarMensaje(HttpServletRequest request, HttpSession session) {
        Object texto = session.getAttribute("vehiculoFlashMensaje");
        if (texto != null) {
            request.setAttribute("mensaje", texto);
            request.setAttribute("tipoMensaje", session.getAttribute("vehiculoFlashTipo"));
            session.removeAttribute("vehiculoFlashMensaje");
            session.removeAttribute("vehiculoFlashTipo");
        }
    }
}
