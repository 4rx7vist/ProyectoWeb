package model.dao;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.*;

public abstract class GenericDAO<T> {

    private static final Logger LOGGER = Logger.getLogger(GenericDAO.class.getName());
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("persistence");
    private final Class<T> entityClass;

    protected GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public boolean create(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(entity);
            transaction.commit();
            return true;
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Couldn't create entity: " + e.getMessage(), e);
            return false;
        } finally {
            em.close();
        }
    }

    public T findById(Object id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(entityClass, id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Couldn't find entity: " + e.getMessage(), e);
            return null;
        } finally {
            em.close();
        }
    }

    public boolean update(T entity) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(entity);
            transaction.commit();
            return true;
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Couldn't update entity: " + e.getMessage(), e);
            return false;
        } finally {
            em.close();
        }
    }

    public boolean remove(Object id) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
                transaction.commit();
                return true;
            }
            LOGGER.log(Level.WARNING, () -> "Entity not found for removal with id: " + id);
            return false;
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Couldn't remove entity: " + e.getMessage(), e);
            return false;
        } finally {
            em.close();
        }
    }

    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Couldn't retrieve entities: " + e.getMessage(), e);
            return List.of();
        } finally {
            em.close();
        }
    }
}