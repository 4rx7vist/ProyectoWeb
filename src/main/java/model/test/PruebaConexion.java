package model.test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.entities.Instructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PruebaConexion {

    private static final Logger logger = LoggerFactory.getLogger(PruebaConexion.class);
    
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("persistence");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            Instructor i = new Instructor();
            i.setCedula("123");
            i.setNombre("Prueba");
            i.setCorreo("prueba@correo.com");
            i.setPassword("123");
            i.setTelefono("0999999999");
            i.setDocumentoEspecialidad(null);
            em.persist(i);
            em.getTransaction().commit();
            logger.info("Instructor creado correctamente.");
        } catch (Exception e) {
            logger.error("Error creando el instructor", e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
            emf.close();
        }
    }
}
