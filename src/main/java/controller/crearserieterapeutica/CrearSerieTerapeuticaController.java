package controller.crearserieterapeutica;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.dao.PosturaDAO;
import model.entities.Instructor;
import model.entities.Postura;
import model.entities.Serie;
import model.service.SerieService;

@WebServlet("/CrearSerieTerapeuticaController")
public class CrearSerieTerapeuticaController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CrearSerieTerapeuticaController.class.getName());

    private transient SerieService serieService = new SerieService();
    private transient PosturaDAO posturaDAO = new PosturaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        router(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        router(req, resp);
    }

    private void router(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Instructor instructor = obtenerInstructor(req, resp);
	if (instructor == null) {
            return;
        }

        String route = req.getParameter("route");
        if (route == null) {
            route = "dashboard";
        }

        switch (route) {
            case "crearSerie":
                crearSerie(req, resp);
                break;
            case "guardar":
                guardar(req, resp, instructor);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/view/dashboard.jsp");
        }
    }

    private Instructor obtenerInstructor(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Object usuario = (session != null) ? session.getAttribute("usuario") : null;

        if (!(usuario instanceof Instructor)) {
            resp.sendRedirect(req.getContextPath() + "/view/inicioSesion.jsp");
            return null;
        }

        return (Instructor) usuario;
    }

    private void crearSerie(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        cargarPosturasEnRequest(req);
        req.getRequestDispatcher("/view/crearSerieTerapeutica.jsp").forward(req, resp);
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp, Instructor instructor) throws ServletException, IOException {
        try {
            validarYGuardarSerie(req, resp, instructor);
        } catch (NumberFormatException e) {
            LOGGER.warning("Error al parsear número de sesiones: " + e.getMessage());
            mostrarErrorConPosturas(req, resp, "Error: número de sesiones inválido");
        } catch (Exception e) {
            LOGGER.severe("Error inesperado al guardar la serie: " + e.getMessage());
            mostrarErrorConPosturas(req, resp, "Error al guardar la serie: " + e.getMessage());
        }
    }

    private void validarYGuardarSerie(HttpServletRequest req, HttpServletResponse resp, Instructor instructor) 
            throws ServletException, IOException {
        String nombreSerie = req.getParameter("nombreSerie");
        String[] posturasSeleccionadas = req.getParameterValues("posturas");
        String sesionesStr = req.getParameter("numSesiones");

        if (!esValido(nombreSerie, posturasSeleccionadas, sesionesStr)) {
            mostrarErrorConPosturas(req, resp, "Faltan datos para crear la serie.");
            return;
        }

        int numeroSesionesRecomendadas = Integer.parseInt(sesionesStr);
        List<String> nombresPosturas = List.of(posturasSeleccionadas);

        Serie serie = serieService.crearSerie(nombreSerie, numeroSesionesRecomendadas, nombresPosturas, instructor);
        
        if (serieService.guardar(serie)) {
            req.setAttribute("serieCreada", serie);
            req.getRequestDispatcher("/view/confirmacion.jsp").forward(req, resp);
        } else {
            mostrarErrorConPosturas(req, resp, "No se pudo guardar la serie.");
        }
    }

    private boolean esValido(String nombreSerie, String[] posturas, String sesiones) {
        return nombreSerie != null && !nombreSerie.isEmpty() &&
               posturas != null && posturas.length > 0 &&
               sesiones != null && !sesiones.isEmpty();
    }

    private void cargarPosturasEnRequest(HttpServletRequest req) {
        List<Postura> posturas = posturaDAO.buscarTodas();
        req.setAttribute("posturas", posturas);
    }

    private void mostrarErrorConPosturas(HttpServletRequest req, HttpServletResponse resp, String mensaje) 
            throws ServletException, IOException {
        req.setAttribute("error", mensaje);
        cargarPosturasEnRequest(req);
        req.getRequestDispatcher("/view/crearSerieTerapeutica.jsp").forward(req, resp);
    }
}
