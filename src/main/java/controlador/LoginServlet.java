package controlador;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import modelo.UsuarioDao;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String usuario = request.getParameter("txtUsuario");
        String password = request.getParameter("txtPassword");

        UsuarioDao dao = new UsuarioDao();
        String resultado = dao.validarLogin(usuario, password);

        switch (resultado) {
            case "EXITO":
                // Se descarta cualquier sesión anterior y se crea una nueva
                HttpSession anterior = request.getSession(false);
                if (anterior != null) {
                    anterior.invalidate();
                }
                HttpSession session = request.getSession(true);
                session.setAttribute("usuarioLogueado", usuario);
                response.sendRedirect("dashboard.jsp");
                break;

            case "BLOQUEADO":
            case "MAX_INTENTOS":
                // Cuenta bloqueada: página que intenta cerrar la ventana
                request.getRequestDispatcher("bloqueado.jsp").forward(request, response);
                break;

            case "ERROR_PASS":
            case "NO_EXISTE":
                mostrarError(request, response,
                        "Usuario o contraseña incorrectos. Verifique sus datos.");
                break;

            case "ERROR_BD":
                mostrarError(request, response,
                        "No se pudo conectar con el sistema. Intente nuevamente en unos minutos.");
                break;

            default:
                mostrarError(request, response,
                        "Ocurrió un error inesperado en el servidor.");
                break;
        }
    }

    // El login solo se procesa por POST; si alguien abre la dirección directamente, vuelve al formulario
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.jsp");
    }

    private void mostrarError(HttpServletRequest request, HttpServletResponse response, String mensaje)
            throws ServletException, IOException {
        request.setAttribute("error", mensaje);
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }
}