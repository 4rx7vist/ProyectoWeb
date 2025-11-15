package model.test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.entities.Instructor;

public class PruebaConexion {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("persistence");
        EntityManager em = emf.createEntityManager();

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

        em.close();
        emf.close();
        System.out.println("Instructor creado correctamente.");
    }
}

