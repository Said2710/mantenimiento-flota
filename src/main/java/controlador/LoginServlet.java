/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        String usuario = request.getParameter("txtUsuario");
        String password = request.getParameter("txtPassword");

        UsuarioDao dao = new UsuarioDao();
        String resultado = dao.validarLogin(usuario, password);

        switch (resultado) {
            case "EXITO":
                HttpSession session = request.getSession();
                session.setAttribute("usuarioLogueado", usuario);
                response.sendRedirect("dashboard.jsp");
                break;
                
            case "BLOQUEADO":
                request.setAttribute("error", "Acceso denegado: Su cuenta se encuentra BLOQUEADA por exceder los 3 intentos fallidos.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
                break;
                
            case "MAX_INTENTOS":
                request.setAttribute("error", "Contraseña incorrecta. Ha alcanzado el límite de 3 intentos y su cuenta ha sido BLOQUEADA.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
                break;
                
            case "ERROR_PASS":
                request.setAttribute("error", "Contraseña incorrecta. Verifique sus datos.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
                break;
                
            case "NO_EXISTE":
                request.setAttribute("error", "El usuario ingresado no existe en el sistema.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
                break;
                
            case "ERROR_BD":
                request.setAttribute("error", "Error de conexión con PostgreSQL. Revisa la contraseña en ConexionBD.java");
                request.getRequestDispatcher("login.jsp").forward(request, response);
                break;
                
            default:
                request.setAttribute("error", "Ocurrió un error inesperado en el servidor.");
                request.getRequestDispatcher("login.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
