package controller.gestionarpaciente;

import java.io.IOException;
import java.io.Serial;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import model.entities.Instructor;
import model.entities.Paciente;
import model.service.PacienteService;

@WebServlet("/GestionarPacienteController")
public class GestionarPacienteController extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE_TYPE_ATTR = "messageType";
    private static final String MESSAGE_ATTR = "message";
    private static final String REDIRECT_LISTAR = "/GestionarPacienteController?route=listarPacientes";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.router(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.router(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void router(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String route = req.getParameter("route");

        switch (route) {
            case "guardar":
                this.registrarPaciente(req, resp);
                break;
            case "registrarPacienteFormulario":
                this.registrarPacienteFormulario(resp);
                break;
            case "listarPacientes":
                this.listarPacientes(req, resp);
                break;
            case "editarPaciente":
                this.editarPaciente(req, resp);
                break;
            case "guardarPacienteExistente":
                this.guardarPacienteExistente(req, resp);
                break;
            case "verHistorial":
                this.verHistorial(req, resp);
                break;
            default:
                throw new IllegalArgumentException("Ruta no encontrada: " + route);
        }
    }

    private void verHistorial(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        PacienteService pacienteService = new PacienteService();
        Paciente paciente = pacienteService.buscarHistorialPorIdPaciente(id);

        req.setAttribute("paciente", paciente);
        req.getRequestDispatcher("view/HISTORIAL_PACIENTE_FORM.jsp").forward(req, resp);
    }

    private void listarPacientes(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Instructor instructor = (Instructor) session.getAttribute("usuario");

        if (instructor == null) {
            resp.sendRedirect("view/inicioSesion.jsp");
            return;
        }

        try {
            List<Paciente> pacientes = new PacienteService().listarPorInstructor(instructor.getCedula());
            req.setAttribute("pacientes", pacientes);
            req.getRequestDispatcher("view/gestionPacientes.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error al listar pacientes", e);
        }
    }

    private void editarPaciente(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        PacienteService pacienteService = new PacienteService();
        Paciente paciente = pacienteService.buscarPacientePorId(id);

        // Formatear fecha de nacimiento
        String fechaFormateada = "";
        if (paciente.getFechaNacimiento() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            fechaFormateada = sdf.format(paciente.getFechaNacimiento());
        }

        // Enviar atributos al JSP
        req.setAttribute("paciente", paciente);
        req.setAttribute("fechaNacimientoFormateada", fechaFormateada);
        req.getRequestDispatcher("view/EDITAR_PACIENTE_FORM.jsp").forward(req, resp);
    }

    private void guardarPacienteExistente(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Paciente paciente = parsePacienteFromRequest(req);
        PacienteService pacienteService = new PacienteService();

        if (pacienteService.guardarExistente(paciente)) {
            setSessionMessage(req, "info", "Paciente actualizado exitosamente");
        } else {
            setSessionMessage(req, "error", "Error al actualizar paciente");
        }
        resp.sendRedirect(req.getContextPath() + REDIRECT_LISTAR);
    }

    private void registrarPacienteFormulario(HttpServletResponse resp) throws IOException {
        resp.sendRedirect("view/registroPaciente.jsp");
    }

    private void registrarPaciente(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Paciente paciente = parsePacienteFromRequest(req);
            PacienteService pacienteService = new PacienteService();

            if (pacienteService.guardar(paciente)) {
                setSessionMessage(req, "info", "Paciente creado exitosamente");
            } else {
                setSessionMessage(req, "error", "Error al registrar paciente");
            }
            resp.sendRedirect(req.getContextPath() + REDIRECT_LISTAR);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("mensaje", "Error al procesar los datos del paciente.");
            req.getRequestDispatcher("view/registroPaciente.jsp").forward(req, resp);
        }
    }

    private Paciente parsePacienteFromRequest(HttpServletRequest req) throws ServletException {
        HttpSession session = req.getSession(false);
        Instructor instructor = (Instructor) session.getAttribute("usuario");

        if (instructor == null) {
            throw new ServletException("Debe iniciar sesión como instructor para registrar pacientes.");
        }

        try {
            String id = req.getParameter("cedula");
            String nombre = req.getParameter("nombre");
            String correo = req.getParameter("correo");
            String contrasena = req.getParameter("contrasena");
            String telefono = req.getParameter("telefono");
            char genero = req.getParameter("genero").charAt(0);
            LocalDate fechaNacimientoLD = LocalDate.parse(req.getParameter("fechaNacimiento"));
            Date fechaNacimiento = java.sql.Date.valueOf(fechaNacimientoLD);

            return new Paciente(
                    id, nombre, correo, telefono, contrasena, fechaNacimiento,
                    genero, instructor, null, null);
        } catch (Exception e) {
            throw new ServletException("Error al parsear los datos del formulario.", e);
        }
    }

    /**
     * Método auxiliar para establecer mensajes en la sesión
     */
    private void setSessionMessage(HttpServletRequest req, String type, String message) {
        HttpSession session = req.getSession();
        session.setAttribute(MESSAGE_TYPE_ATTR, type);
        session.setAttribute(MESSAGE_ATTR, message);
    }
}
