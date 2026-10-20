package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import modelo.Usuario;
import modelo.UsuarioRepository;
import modelo.UsuarioRepositoryJdbc;

abstract class AdministradorServletBase extends HttpServlet {

    private final UsuarioRepository usuarios = new UsuarioRepositoryJdbc();

    protected Usuario validarAdministrador(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String nombre = session == null ? null : (String) session.getAttribute("usuarioLogueado");
        if (nombre == null) {
            response.sendRedirect("login.jsp");
            return null;
        }
        Usuario usuario = usuarios.buscarPorNombre(nombre);
        if (usuario == null || !usuario.esAdministrador()) {
            session.invalidate();
            request.setAttribute("error", "Tu cuenta no tiene permiso para gestionar ventas.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return null;
        }
        return usuario;
    }
}
