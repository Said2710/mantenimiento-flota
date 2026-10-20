package controlador;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import modelo.Componente;
import modelo.ComponenteRepository;
import modelo.ComponenteRepositoryJdbc;
import modelo.ReglaMantenimiento;
import modelo.ReglaMantenimientoRepository;
import modelo.ReglaMantenimientoRepositoryJdbc;
import modelo.Usuario;
import modelo.UsuarioRepository;
import modelo.UsuarioRepositoryJdbc;

@WebServlet(name = "ReglaMantenimientoServlet", urlPatterns = {"/ReglaMantenimientoServlet"})
public class ReglaMantenimientoServlet extends HttpServlet {

    private final ReglaMantenimientoRepository reglas = new ReglaMantenimientoRepositoryJdbc();
    private final ComponenteRepository componentes = new ComponenteRepositoryJdbc();
    private final UsuarioRepository usuarios = new UsuarioRepositoryJdbc();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (validarAdministrador(request, response, session) == null) return;

        List<ReglaMantenimiento> listaReglas = reglas.listar();
        List<Componente> listaComponentes = componentes.listar();
        if (listaReglas == null || listaComponentes == null) {
            listaReglas = listaReglas == null ? new ArrayList<>() : listaReglas;
            listaComponentes = listaComponentes == null ? new ArrayList<>() : listaComponentes;
            request.setAttribute("mensaje", "No se pudieron cargar las reglas o los componentes.");
            request.setAttribute("tipoMensaje", "danger");
        } else {
            recuperarMensaje(request, session);
        }

        String idEditar = request.getParameter("editar");
        if (idEditar != null) {
            try {
                ReglaMantenimiento regla = reglas.buscarPorId(Integer.parseInt(idEditar));
                if (regla != null) request.setAttribute("reglaEditar", regla);
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador de la regla no es válido.");
                recuperarMensaje(request, session);
            }
        }

        request.setAttribute("reglas", listaReglas);
        request.setAttribute("componentes", listaComponentes);
        request.getRequestDispatcher("reglas-mantenimiento.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (validarAdministrador(request, response, session) == null) return;

        String accion = request.getParameter("accion");
        if ("crear".equals(accion)) {
            guardar(request, session, false);
        } else if ("editar".equals(accion)) {
            guardar(request, session, true);
        } else if ("desactivar".equals(accion)) {
            desactivar(request, session);
        } else {
            mensaje(session, "danger", "La acción solicitada no es válida.");
        }
        response.sendRedirect("ReglaMantenimientoServlet");
    }

    private void guardar(HttpServletRequest request, HttpSession session, boolean editar) {
        ReglaMantenimiento regla;
        try {
            regla = leerRegla(request);
        } catch (IllegalArgumentException e) {
            mensaje(session, "danger", e.getMessage());
            return;
        }

        String error = validar(regla);
        if (error != null) {
            mensaje(session, "danger", error);
            return;
        }

        String resultado;
        if (editar) {
            try {
                regla.setIdRegla(Integer.parseInt(request.getParameter("idRegla")));
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador de la regla no es válido.");
                return;
            }
            resultado = reglas.editar(regla);
        } else {
            resultado = reglas.crear(regla);
        }

        if (ReglaMantenimientoRepository.OK.equals(resultado)) {
            mensaje(session, "success", editar ? "Regla actualizada." : "Regla registrada.");
        } else if (ReglaMantenimientoRepository.NO_ENCONTRADO.equals(resultado)) {
            mensaje(session, "warning", "No se encontró la regla.");
        } else {
            mensaje(session, "danger", "No se pudo guardar la regla. Revisa la conexión con la base de datos.");
        }
    }

    private ReglaMantenimiento leerRegla(HttpServletRequest request) {
        ReglaMantenimiento regla = new ReglaMantenimiento();
        try {
            regla.setIdComponente(Integer.parseInt(request.getParameter("idComponente")));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Selecciona un componente válido.");
        }

        String nombre = limpiar(request.getParameter("nombre"));
        String tipo = limpiar(request.getParameter("tipoMetrica"));
        String valorIntervalo = limpiar(request.getParameter("intervalo"));
        try {
            regla.setIntervalo(new BigDecimal(valorIntervalo).setScale(2, RoundingMode.UNNECESSARY));
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException("El intervalo debe ser un número válido con máximo dos decimales.");
        }

        regla.setNombre(nombre);
        regla.setTipoMetrica(tipo);
        regla.setDescripcion(limpiar(request.getParameter("descripcion")));
        String activo = request.getParameter("activo");
        if (!"true".equals(activo) && !"false".equals(activo)) {
            throw new IllegalArgumentException("Selecciona un estado válido para la regla.");
        }
        regla.setActivo(Boolean.parseBoolean(activo));
        return regla;
    }

    private String validar(ReglaMantenimiento regla) {
        if (regla.getNombre().isBlank()) return "El nombre de la regla es obligatorio.";
        if (regla.getNombre().length() > 100 || regla.getDescripcion().length() > 300) {
            return "Uno de los campos supera el máximo permitido por la base de datos.";
        }
        if (regla.getIntervalo().signum() <= 0 || regla.getIntervalo().precision() > 12) {
            return "El intervalo debe ser positivo y no superar el máximo permitido.";
        }
        if (!"KILOMETRAJE".equals(regla.getTipoMetrica())
                && !"HORAS_MOTOR".equals(regla.getTipoMetrica())
                && !"DIAS".equals(regla.getTipoMetrica())) {
            return "Selecciona una métrica válida.";
        }
        Componente componente = componentes.buscarPorId(regla.getIdComponente());
        if (componente == null) return "El componente seleccionado no existe.";
        if (regla.isActivo() && !"Activo".equals(componente.getEstado())) {
            return "Activa el componente seleccionado antes de activar esta regla.";
        }
        return null;
    }

    private void desactivar(HttpServletRequest request, HttpSession session) {
        try {
            String resultado = reglas.desactivar(Integer.parseInt(request.getParameter("idRegla")));
            if (ReglaMantenimientoRepository.OK.equals(resultado)) {
                mensaje(session, "success", "Regla desactivada.");
            } else if (ReglaMantenimientoRepository.NO_ENCONTRADO.equals(resultado)) {
                mensaje(session, "warning", "No se encontró la regla.");
            } else {
                mensaje(session, "danger", "No se pudo desactivar la regla.");
            }
        } catch (NumberFormatException e) {
            mensaje(session, "danger", "El identificador de la regla no es válido.");
        }
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
            request.setAttribute("error", "Tu cuenta no tiene permiso para gestionar reglas.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return null;
        }
        return actual;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private void mensaje(HttpSession session, String tipo, String texto) {
        session.setAttribute("reglaFlashTipo", tipo);
        session.setAttribute("reglaFlashMensaje", texto);
    }

    private void recuperarMensaje(HttpServletRequest request, HttpSession session) {
        Object texto = session.getAttribute("reglaFlashMensaje");
        if (texto != null) {
            request.setAttribute("mensaje", texto);
            request.setAttribute("tipoMensaje", session.getAttribute("reglaFlashTipo"));
            session.removeAttribute("reglaFlashMensaje");
            session.removeAttribute("reglaFlashTipo");
        }
    }
}
