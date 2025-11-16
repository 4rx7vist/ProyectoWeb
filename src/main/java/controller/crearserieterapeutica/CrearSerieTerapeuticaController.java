package controller.crearserieterapeutica;

import java.io.IOException;
import java.util.List;

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

    // ================== CONSTANTES PARA STRINGS DUPLICADOS ==================
    private static final String ATTR_POSTURAS = "posturas";
    private static final String ATTR_ERROR = "error";
    private static final String VIEW_CREAR = "/view/crearSerieTerapeutica.jsp";
    private static final String VIEW_CONFIRMACION = "/view/confirmacion.jsp";

    private transient final SerieService serieService = new SerieService();
    private transient final PosturaDAO posturaDAO = new PosturaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.router(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.router(req, resp);
    }

    private void router(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Object usuario = (session != null) ? session.getAttribute("usuario") : null;

        if (!(usuario instanceof Instructor)) {
            resp.sendRedirect(req.getContextPath() + "/view/inicioSesion.jsp");
            return;
        }

        Instructor instructor = (Instructor) usuario;

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

    private void crearSerie(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Postura> posturas = posturaDAO.buscarTodas();
        req.setAttribute(ATTR_POSTURAS, posturas);
        req.getRequestDispatcher(VIEW_CREAR).forward(req, resp);
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp, Instructor instructor) throws ServletException, IOException {
        try {
            String nombreSerie = req.getParameter("nombreSerie");
            String[] posturasSeleccionadas = req.getParameterValues(ATTR_POSTURAS);
            String sesionesStr = req.getParameter("numSesiones");

            if (nombreSerie == null || posturasSeleccionadas == null || sesionesStr == null ||
                nombreSerie.isEmpty() || sesionesStr.isEmpty()) {

                req.setAttribute(ATTR_ERROR, "Faltan datos para crear la serie.");
                List<Postura> posturas = posturaDAO.buscarTodas();
                req.setAttribute(ATTR_POSTURAS, posturas);
                req.getRequestDispatcher(VIEW_CREAR).forward(req, resp);
                return;
            }

            int numeroSesionesRecomendadas = Integer.parseInt(sesionesStr);
            List<String> nombresPosturas = List.of(posturasSeleccionadas);

            Serie serie = serieService.crearSerie(
                nombreSerie,
                numeroSesionesRecomendadas,
                nombresPosturas,
                instructor
            );

            boolean guardadoExitoso = serieService.guardar(serie);

            if (guardadoExitoso) {
                req.setAttribute("serieCreada", serie);
                req.getRequestDispatcher(VIEW_CONFIRMACION).forward(req, resp);
            } else {
                req.setAttribute(ATTR_ERROR, "No se pudo guardar la serie.");
                List<Postura> posturas = posturaDAO.buscarTodas();
                req.setAttribute(ATTR_POSTURAS, posturas);
                req.getRequestDispatcher(VIEW_CREAR).forward(req, resp);
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute(ATTR_ERROR, "Error al guardar la serie: " + e.getMessage());
            List<Postura> posturas = posturaDAO.buscarTodas();
            req.setAttribute(ATTR_POSTURAS, posturas);
            req.getRequestDispatcher(VIEW_CREAR).forward(req, resp);
        }
    }
}
