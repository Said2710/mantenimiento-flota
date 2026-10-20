package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import modelo.EvaluacionMantenimiento;
import modelo.EvaluacionMantenimientoRepository;
import modelo.EvaluacionMantenimientoRepositoryJdbc;
import modelo.Usuario;
import modelo.UsuarioRepository;
import modelo.UsuarioRepositoryJdbc;

@WebServlet(name = "EvaluacionMantenimientoServlet", urlPatterns = {"/EvaluacionMantenimientoServlet"})
public class EvaluacionMantenimientoServlet extends HttpServlet {

    private final EvaluacionMantenimientoRepository evaluaciones = new EvaluacionMantenimientoRepositoryJdbc();
    private final UsuarioRepository usuarios = new UsuarioRepositoryJdbc();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!validarAdministrador(request, response, session)) return;

        List<EvaluacionMantenimiento> lista = evaluaciones.evaluar();
        if (lista == null) {
            lista = new ArrayList<>();
            request.setAttribute("mensaje", "No se pudo calcular el estado preventivo. Revisa la conexión y el esquema de Azure.");
            request.setAttribute("tipoMensaje", "danger");
        }
        request.setAttribute("evaluaciones", lista);
        request.getRequestDispatcher("evaluacion-mantenimiento.jsp").forward(request, response);
    }

    private boolean validarAdministrador(HttpServletRequest request,
                                         HttpServletResponse response,
                                         HttpSession session)
            throws ServletException, IOException {
        String nombre = session == null ? null : (String) session.getAttribute("usuarioLogueado");
        if (nombre == null) {
            response.sendRedirect("login.jsp");
            return false;
        }
        Usuario actual = usuarios.buscarPorNombre(nombre);
        if (actual == null || !actual.esAdministrador()) {
            session.invalidate();
            request.setAttribute("error", "Tu cuenta no tiene permiso para consultar la evaluación preventiva.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return false;
        }
        return true;
    }
}
