package controller.registrarpaciente;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import model.dao.PacienteDAO;
import model.entities.Paciente;

@WebServlet("/RegistrarPacienteController")
public class RegistrarPacienteController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String id = request.getParameter("cedula");
		String nombre = request.getParameter("nombre");
		String correo = request.getParameter("correo");
		String telefono = request.getParameter("telefono");
		String fechaStr = request.getParameter("fechaNacimiento");
		String contrasena = request.getParameter("contrasena"); // ✅ ahora viene del formulario

		Date fechaNacimiento = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			fechaNacimiento = sdf.parse(fechaStr);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Paciente paciente = new Paciente();
		paciente.setId(id);
		paciente.setNombre(nombre);
		paciente.setCorreo(correo);
		paciente.setTelefono(telefono);
		paciente.setFechaNacimiento(fechaNacimiento);
		paciente.setContraseña(contrasena); 
		paciente.setGenero('N'); 
		paciente.setInstructor(null);
		paciente.setSerieAsignada(null);
		paciente.setHistorialSesiones(null);

		PacienteDAO dao = new PacienteDAO();
		boolean exito = dao.create(paciente);

		if (exito) {
			response.sendRedirect("registro_exitoso.jsp");
		} else {
			response.sendRedirect("registro_fallido.jsp");
		}
	}
}
