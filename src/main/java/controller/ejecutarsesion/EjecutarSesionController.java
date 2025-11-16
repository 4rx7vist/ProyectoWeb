package controller.ejecutarsesion;

import java.io.IOException;
import java.io.Serial;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.dao.PacienteDAO;
import model.entities.Paciente;
import model.entities.Postura;
import model.entities.Serie;
import model.entities.Sesion;
import model.service.SesionService;

@WebServlet("/EjecutarSesionController")
public class EjecutarSesionController extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    
    private static final String ATTR_PACIENTE = "paciente";
    private static final String ATTR_POSTURAS_SESION = "posturasDeLaSesion";
    private static final String ATTR_INDICE_POSTURA_ACTUAL = "indicePosturaActual";
    private static final String ATTR_DOLOR_INICIAL = "dolorInicial";
    private static final String ATTR_POSTURA_ACTUAL = "posturaActual";
    private static final String ATTR_NUMERO_POSTURA_ACTUAL = "numeroPosturaActual";
    private static final String ATTR_TOTAL_POSTURAS = "totalPosturas";
    private static final String ATTR_POSTURA_DETALLADA = "posturaDetallada";
    
    private static final String VIEW_DASHBOARD_PACIENTE = "/view/dashboardPaciente.jsp";
    private static final String VIEW_DOLOR_INICIAL = "/view/dolorInicial.jsp";
    private static final String VIEW_POSTURA_EJECUCION = "/view/posturaEjecucion.jsp";
    private static final String VIEW_EVALUACION_FINAL = "/view/evaluacionFinal.jsp";
    private static final String VIEW_VISUALIZAR_POSTURAS = "/view/visualizarPosturas.jsp";
    
    private static final String PARAM_ROUTE = "route";
    private static final String ROUTE_DASHBOARD = "dashboardPaciente";

    private final transient SesionService sesionService = new SesionService();
    private final transient Map<String, RouteAction> routeActions = new HashMap<>();

    @FunctionalInterface
    private interface RouteAction {
        void execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
    }

    public EjecutarSesionController() {
        super();
        initializeRoutes();
    }

    private void initializeRoutes() {
        routeActions.put(ROUTE_DASHBOARD, this::mostrarDashboard);
        routeActions.put("iniciar", this::mostrarDolorInicial);
        routeActions.put("registrarDolor", this::registrarDolorEIniciar);
        routeActions.put("siguientePostura", this::avanzarSiguientePostura);
        routeActions.put("verInstrucciones", this::mostrarInstrucciones);
        routeActions.put("volverAEjecucion", this::volverAEjecucion);
        routeActions.put("registrarEvaluacion", this::registrarEvaluacionFinal);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.router(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.router(req, resp);
    }

    private void router(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String route = req.getParameter(PARAM_ROUTE);
        if (route == null) {
            route = ROUTE_DASHBOARD;
        }

        actualizarPacienteEnSesion(req);
        
        RouteAction action = routeActions.getOrDefault(route, this::redirigirDashboard);
        action.execute(req, resp);
    }

    private void actualizarPacienteEnSesion(HttpServletRequest req) {
        HttpSession session = req.getSession();
        Paciente paciente = (Paciente) session.getAttribute(ATTR_PACIENTE);

        if (paciente != null) {
            PacienteDAO pacienteDAO = new PacienteDAO();
            paciente = pacienteDAO.getPacienteConSerieYPosturas(paciente.getId());
            session.setAttribute(ATTR_PACIENTE, paciente);
        }
    }

    private void mostrarDashboard(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher(VIEW_DASHBOARD_PACIENTE).forward(req, resp);
    }

    private void mostrarDolorInicial(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher(VIEW_DOLOR_INICIAL).forward(req, resp);
    }

    private void redirigirDashboard(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + VIEW_DASHBOARD_PACIENTE);
    }

    private void registrarDolorEIniciar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        session.setAttribute(ATTR_DOLOR_INICIAL, req.getParameter("dolorInicio"));

        Paciente paciente = (Paciente) session.getAttribute(ATTR_PACIENTE);
        Serie serie = paciente.getSerieAsignada();

        if (validarSerieConPosturas(serie)) {
            iniciarSesionConPosturas(req, resp, session, serie.getPosturas());
        } else {
            redirigirDashboard(req, resp);
        }
    }

    private boolean validarSerieConPosturas(Serie serie) {
        return serie != null && serie.getPosturas() != null && !serie.getPosturas().isEmpty();
    }

    private void iniciarSesionConPosturas(HttpServletRequest req, HttpServletResponse resp, HttpSession session, List<Postura> posturas) throws ServletException, IOException {
        session.setAttribute(ATTR_POSTURAS_SESION, posturas);
        session.setAttribute(ATTR_INDICE_POSTURA_ACTUAL, 0);
        configurarVistaPostura(req, posturas, 0);
        req.getRequestDispatcher(VIEW_POSTURA_EJECUCION).forward(req, resp);
    }

    private void avanzarSiguientePostura(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        List<Postura> posturas = (List<Postura>) session.getAttribute(ATTR_POSTURAS_SESION);
        int indiceActual = (int) session.getAttribute(ATTR_INDICE_POSTURA_ACTUAL);

        int indiceSiguiente = indiceActual + 1;

        if (indiceSiguiente < posturas.size()) {
            session.setAttribute(ATTR_INDICE_POSTURA_ACTUAL, indiceSiguiente);
            configurarVistaPostura(req, posturas, indiceSiguiente);
            req.getRequestDispatcher(VIEW_POSTURA_EJECUCION).forward(req, resp);
        } else {
            req.getRequestDispatcher(VIEW_EVALUACION_FINAL).forward(req, resp);
        }
    }

    private void registrarEvaluacionFinal(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();

        Paciente paciente = (Paciente) session.getAttribute(ATTR_PACIENTE);
        Serie serie = paciente.getSerieAsignada();
        String dolorInicial = (String) session.getAttribute(ATTR_DOLOR_INICIAL);

        Sesion nuevaSesion = crearNuevaSesion(paciente, serie, dolorInicial, req);
        sesionService.guardar(nuevaSesion);

        session.invalidate();
        resp.sendRedirect(req.getContextPath() + "/LoginController?" + PARAM_ROUTE + "=login");
    }

    private Sesion crearNuevaSesion(Paciente paciente, Serie serie, String dolorInicial, HttpServletRequest req) {
        Sesion nuevaSesion = new Sesion();
        nuevaSesion.setPaciente(paciente);
        nuevaSesion.setSerie(serie);
        nuevaSesion.setFecha(new Date());
        nuevaSesion.setDolorInicial(dolorInicial);
        nuevaSesion.setDolorFinal(req.getParameter("dolorFinal"));
        nuevaSesion.setComentario(req.getParameter("comentario"));
        return nuevaSesion;
    }

    private void mostrarInstrucciones(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        List<Postura> posturas = (List<Postura>) session.getAttribute(ATTR_POSTURAS_SESION);
        int indiceActual = (int) session.getAttribute(ATTR_INDICE_POSTURA_ACTUAL);

        req.setAttribute(ATTR_POSTURA_DETALLADA, posturas.get(indiceActual));
        req.getRequestDispatcher(VIEW_VISUALIZAR_POSTURAS).forward(req, resp);
    }

    private void volverAEjecucion(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        List<Postura> posturas = (List<Postura>) session.getAttribute(ATTR_POSTURAS_SESION);
        int indiceActual = (int) session.getAttribute(ATTR_INDICE_POSTURA_ACTUAL);

        configurarVistaPostura(req, posturas, indiceActual);
        req.getRequestDispatcher(VIEW_POSTURA_EJECUCION).forward(req, resp);
    }

    private void configurarVistaPostura(HttpServletRequest req, List<Postura> posturas, int indice) {
        req.setAttribute(ATTR_POSTURA_ACTUAL, posturas.get(indice));
        req.setAttribute(ATTR_NUMERO_POSTURA_ACTUAL, indice + 1);
        req.setAttribute(ATTR_TOTAL_POSTURAS, posturas.size());
    }
}
