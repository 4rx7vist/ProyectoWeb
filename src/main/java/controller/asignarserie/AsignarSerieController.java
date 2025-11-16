package controller.asignarserie;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

import model.dao.PacienteDAO;
import model.dao.SerieDAO;
import model.entities.Instructor;
import model.entities.Paciente;
import model.entities.Serie;

@WebServlet("/AsignarSerieController")
public class AsignarSerieController extends HttpServlet {

    private static final long serialVersionUID = 1L;


    //CONSTANTES INTERNAS

    private static final String USUARIO = "usuario";
    private static final String ASIGNACION_EXITOSA = "asignacionExitosa";
    private static final String MESSAGE_TYPE = "messageType";
    private static final String MESSAGE = "message";
    private static final String WARNING = "warning";
    private static final String SUCCESS = "success";
    private static final String ROUTE = "route";

    // MÉTODOS

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        router(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        router(request, response);
    }

    private void router(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Instructor instructor = (session != null) ? (Instructor) session.getAttribute(USUARIO) : null;

        if (instructor == null) {
            response.sendRedirect(request.getContextPath() + "/LoginController?route=entrar");
            return;
        }

        String route = request.getParameter(ROUTE);

        switch (route == null ? "listar" : route) {
            case "listar":
                listarPacientes(request);
                listarSeries(request);
                request.getRequestDispatcher("view/AsignarSerie.jsp").forward(request, response);
                break;

            case "asignar":
                asignarSerie(request, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Ruta no válida: " + route);
        }
    }


    private void listarPacientes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Instructor instructor = (session != null) ? (Instructor) session.getAttribute(USUARIO) : null;

        if (instructor != null) {
            String instructorId = instructor.getCedula();
            PacienteDAO pacienteDAO = new PacienteDAO();
            List<Paciente> pacientes = pacienteDAO.getPacientesByInstructor(instructorId);
            request.setAttribute("pacientes", pacientes);
        }
    }

    private void listarSeries(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Instructor instructor = (session != null) ? (Instructor) session.getAttribute(USUARIO) : null;

        if (instructor != null) {
            String instructorId = instructor.getCedula();
            SerieDAO serieDAO = new SerieDAO();
            List<Serie> series = serieDAO.getSeriesByInstructor(instructorId);
            request.setAttribute("series", series);
        }
    }



    private void asignarSerie(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pacienteId = request.getParameter("pacienteId");
        String serieId = request.getParameter("serieId");

        if (pacienteId == null || serieId == null) {

            request.setAttribute(ASIGNACION_EXITOSA, false);
            request.setAttribute(MESSAGE_TYPE, WARNING);
            request.setAttribute(MESSAGE, " Faltan parámetros para asignar la serie.");

        } else {

            PacienteDAO pacienteDAO = new PacienteDAO();

            if (!pacienteDAO.tieneSerieAsignada(pacienteId)) {

                boolean resultado = pacienteDAO.guardarSerie(pacienteId, serieId);

                if (resultado) {
                    request.setAttribute(ASIGNACION_EXITOSA, true);
                    request.setAttribute(MESSAGE_TYPE, SUCCESS);
                    request.setAttribute(MESSAGE, " Se asignó correctamente la serie al paciente.");
                } else {
                    request.setAttribute(ASIGNACION_EXITOSA, false);
                    request.setAttribute(MESSAGE_TYPE, WARNING);
                    request.setAttribute(MESSAGE, " Error al asignar la serie.");
                }

            } else {
                request.setAttribute(ASIGNACION_EXITOSA, false);
                request.setAttribute(MESSAGE_TYPE, WARNING);
                request.setAttribute(MESSAGE, " El paciente ya tiene una serie asignada.");
            }
        }

        request.getRequestDispatcher("view/mensaje.jsp").forward(request, response);
    }
}
