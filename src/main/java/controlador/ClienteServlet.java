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
import modelo.Cliente;
import modelo.ClienteRepository;
import modelo.ClienteRepositoryJdbc;
import modelo.Usuario;
import modelo.UsuarioRepository;
import modelo.UsuarioRepositoryJdbc;

@WebServlet(name = "ClienteServlet", urlPatterns = {"/ClienteServlet"})
public class ClienteServlet extends HttpServlet {

    private final ClienteRepository clientes = new ClienteRepositoryJdbc();
    private final UsuarioRepository usuarios = new UsuarioRepositoryJdbc();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Usuario actual = validarAdministrador(request, response, session);
        if (actual == null) {
            return;
        }

        List<Cliente> lista = clientes.listar();
        if (lista == null) {
            lista = new ArrayList<>();
            request.setAttribute("mensaje", "No se pudo cargar la lista de clientes.");
            request.setAttribute("tipoMensaje", "danger");
        } else {
            recuperarMensaje(request, session);
        }

        String idEditar = request.getParameter("editar");
        if (idEditar != null) {
            try {
                Cliente cliente = clientes.buscarPorId(Integer.parseInt(idEditar));
                if (cliente != null) {
                    request.setAttribute("clienteEditar", cliente);
                }
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador del cliente no es válido.");
                recuperarMensaje(request, session);
            }
        }

        request.setAttribute("clientes", lista);
        request.setAttribute("usuarioLogueado", actual.getNombreUsuario());
        request.getRequestDispatcher("clientes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Usuario actual = validarAdministrador(request, response, session);
        if (actual == null) {
            return;
        }

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

        response.sendRedirect("ClienteServlet");
    }

    private void guardar(HttpServletRequest request, HttpSession session,
                         Usuario actual, boolean editar) {
        Cliente cliente = leerCliente(request);
        String error = validar(cliente);
        if (error != null) {
            mensaje(session, "danger", error);
            return;
        }

        String resultado;
        if (editar) {
            try {
                cliente.setIdCliente(Integer.parseInt(request.getParameter("idCliente")));
            } catch (NumberFormatException e) {
                mensaje(session, "danger", "El identificador del cliente no es válido.");
                return;
            }
            resultado = clientes.editar(cliente);
        } else {
            cliente.setIdUsuarioRegistro(actual.getIdUsuario());
            resultado = clientes.crear(cliente);
        }

        if (ClienteRepository.OK.equals(resultado)) {
            mensaje(session, "success", editar ? "Cliente actualizado." : "Cliente registrado.");
        } else if (ClienteRepository.DUPLICADO.equals(resultado)) {
            mensaje(session, "warning", "Ya existe un dato igual registrado.");
        } else if (ClienteRepository.NO_ENCONTRADO.equals(resultado)) {
            mensaje(session, "warning", "No se encontró el cliente.");
        } else {
            mensaje(session, "danger", "No se pudo guardar el cliente. Revisa la conexión con la base de datos.");
        }
    }

    private void inactivar(HttpServletRequest request, HttpSession session) {
        try {
            int idCliente = Integer.parseInt(request.getParameter("idCliente"));
            String resultado = clientes.eliminar(idCliente);
            if (ClienteRepository.OK.equals(resultado)) {
                mensaje(session, "success", "Cliente inactivado.");
            } else if (ClienteRepository.NO_ENCONTRADO.equals(resultado)) {
                mensaje(session, "warning", "No se encontró el cliente.");
            } else {
                mensaje(session, "danger", "No se pudo inactivar el cliente.");
            }
        } catch (NumberFormatException e) {
            mensaje(session, "danger", "El identificador del cliente no es válido.");
        }
    }

    private Cliente leerCliente(HttpServletRequest request) {
        Cliente cliente = new Cliente();
        cliente.setRazonSocial(limpiar(request.getParameter("razonSocial")));
        cliente.setNumeroDocumento(limpiar(request.getParameter("numeroDocumento")));
        cliente.setCorreo(limpiar(request.getParameter("correo")));
        cliente.setTelefono(limpiar(request.getParameter("telefono")));
        cliente.setDireccion(limpiar(request.getParameter("direccion")));
        cliente.setEstado(limpiar(request.getParameter("estado")));
        return cliente;
    }

    private String validar(Cliente cliente) {
        if (cliente.getRazonSocial().isBlank()) {
            return "La razón social es obligatoria.";
        }
        if (supera(cliente.getRazonSocial(), 150)
                || supera(cliente.getNumeroDocumento(), 20)
                || supera(cliente.getCorreo(), 150)
                || supera(cliente.getTelefono(), 30)
                || supera(cliente.getDireccion(), 250)) {
            return "Uno de los campos supera el máximo permitido por la base de datos.";
        }
        if (!"Activo".equals(cliente.getEstado()) && !"Inactivo".equals(cliente.getEstado())) {
            return "Selecciona un estado válido.";
        }
        return null;
    }

    private boolean supera(String valor, int maximo) {
        return valor != null && valor.length() > maximo;
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
            request.setAttribute("error", "Tu cuenta no tiene permiso para gestionar clientes.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return null;
        }
        return actual;
    }

    private void mensaje(HttpSession session, String tipo, String texto) {
        session.setAttribute("clienteFlashTipo", tipo);
        session.setAttribute("clienteFlashMensaje", texto);
    }

    private void recuperarMensaje(HttpServletRequest request, HttpSession session) {
        Object texto = session.getAttribute("clienteFlashMensaje");
        if (texto != null) {
            request.setAttribute("mensaje", texto);
            request.setAttribute("tipoMensaje", session.getAttribute("clienteFlashTipo"));
            session.removeAttribute("clienteFlashMensaje");
            session.removeAttribute("clienteFlashTipo");
        }
    }
}
