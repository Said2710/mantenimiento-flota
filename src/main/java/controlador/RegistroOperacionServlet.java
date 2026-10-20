package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import modelo.RegistroOperacion;
import modelo.RegistroOperacionRepository;
import modelo.RegistroOperacionRepositoryJdbc;
import modelo.Usuario;
import modelo.UsuarioRepository;
import modelo.UsuarioRepositoryJdbc;
import modelo.Vehiculo;
import modelo.VehiculoRepository;
import modelo.VehiculoRepositoryJdbc;

@WebServlet(name = "RegistroOperacionServlet", urlPatterns = {"/RegistroOperacionServlet"})
public class RegistroOperacionServlet extends HttpServlet {

    private final RegistroOperacionRepository registros = new RegistroOperacionRepositoryJdbc();
    private final VehiculoRepository vehiculos = new VehiculoRepositoryJdbc();
    private final UsuarioRepository usuarios = new UsuarioRepositoryJdbc();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario actual = validarAdministrador(request, response, session);
        if (actual == null) return;

        List<RegistroOperacion> lista = registros.listar();
        List<Vehiculo> unidades = vehiculos.listar();
        if (lista == null || unidades == null) {
            lista = lista == null ? new ArrayList<>() : lista;
            unidades = unidades == null ? new ArrayList<>() : unidades;
            request.setAttribute("mensaje", "No se pudieron cargar las lecturas o los vehículos.");
            request.setAttribute("tipoMensaje", "danger");
        } else {
            recuperarMensaje(request, session);
        }
        unidades.removeIf(v -> !"Operativo".equals(v.getEstado()));
        request.setAttribute("registros", lista);
        request.setAttribute("vehiculos", unidades);
        request.getRequestDispatcher("registro-operacion.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Usuario actual = validarAdministrador(request, response, session);
        if (actual == null) return;

        try {
            RegistroOperacion registro = leerRegistro(request);
            String error = validar(registro);
            if (error != null) {
                mensaje(session, "danger", error);
            } else {
                registro.setIdUsuarioRegistro(actual.getIdUsuario());
                String resultado = registros.crear(registro);
                mensaje(session, "OK".equals(resultado) ? "success" : "danger",
                        "OK".equals(resultado) ? "Lectura registrada." : "No se pudo guardar la lectura.");
            }
        } catch (IllegalArgumentException e) {
            mensaje(session, "danger", e.getMessage());
        }
        response.sendRedirect("RegistroOperacionServlet");
    }

    private RegistroOperacion leerRegistro(HttpServletRequest request) {
        RegistroOperacion registro = new RegistroOperacion();
        try {
            registro.setIdVehiculo(Integer.parseInt(limpiar(request.getParameter("idVehiculo"))));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Selecciona un vehículo válido.");
        }
        registro.setKilometraje(decimal(request.getParameter("kilometraje"), "kilometraje"));
        registro.setHorasMotor(decimal(request.getParameter("horasMotor"), "horas de motor"));
        registro.setObservacion(limpiar(request.getParameter("observacion")));
        return registro;
    }

    private BigDecimal decimal(String valor, String campo) {
        try {
            return new BigDecimal(limpiar(valor)).setScale(2, RoundingMode.UNNECESSARY);
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("Ingresa " + campo + " como número válido con máximo dos decimales.");
        }
    }

    private String validar(RegistroOperacion registro) {
        if (registro.getKilometraje().signum() < 0 || registro.getHorasMotor().signum() < 0) {
            return "Kilometraje y horas de motor deben ser cero o mayores.";
        }
        if (registro.getKilometraje().precision() > 12 || registro.getHorasMotor().precision() > 12) {
            return "Una lectura supera el máximo permitido.";
        }
        if (registro.getObservacion().length() > 300) return "La observación supera los 300 caracteres.";

        Vehiculo vehiculo = vehiculos.buscarPorId(registro.getIdVehiculo());
        if (vehiculo == null || !"Operativo".equals(vehiculo.getEstado())) {
            return "El vehículo no existe o no está operativo.";
        }

        RegistroOperacion anterior = registros.ultimoPorVehiculo(registro.getIdVehiculo());
        if (anterior != null && registro.getKilometraje().compareTo(anterior.getKilometraje()) < 0) {
            return "El kilometraje no puede ser menor que la lectura anterior.";
        }
        if (anterior != null && registro.getHorasMotor().compareTo(anterior.getHorasMotor()) < 0) {
            return "Las horas de motor no pueden ser menores que la lectura anterior.";
        }
        return null;
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
            request.setAttribute("error", "Tu cuenta no tiene permiso para registrar lecturas.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return null;
        }
        return actual;
    }

    private String limpiar(String valor) { return valor == null ? "" : valor.trim(); }

    private void mensaje(HttpSession session, String tipo, String texto) {
        session.setAttribute("lecturaFlashTipo", tipo);
        session.setAttribute("lecturaFlashMensaje", texto);
    }

    private void recuperarMensaje(HttpServletRequest request, HttpSession session) {
        Object texto = session.getAttribute("lecturaFlashMensaje");
        if (texto != null) {
            request.setAttribute("mensaje", texto);
            request.setAttribute("tipoMensaje", session.getAttribute("lecturaFlashTipo"));
            session.removeAttribute("lecturaFlashMensaje");
            session.removeAttribute("lecturaFlashTipo");
        }
    }
}
