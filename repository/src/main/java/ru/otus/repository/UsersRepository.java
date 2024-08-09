package ru.otus.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.repository.entities.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

public class UsersRepository implements AutoCloseable {

    private static final String PERSISTENCE_UNIT_NAME = "USERS";
    private static EntityManagerFactory factory;
    private static final Logger logger = LoggerFactory.getLogger(UsersRepository.class);
    private final EntityManager em;

    public UsersRepository() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        em = factory.createEntityManager();
    }

    public List<User> getUsersByRole(RoleEnum role) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cr = cb.createQuery(User.class);
        Root<User> root = cr.from(User.class);
        Join<User, Role> joinedRole = root.join("role");
        cr.select(root).where(cb.equal(joinedRole.get("name"), role));
        return em.createQuery(cr).getResultList();
    }


    public List<User> getUsers(boolean onlyActive) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cr = cb.createQuery(User.class);
        Root<User> root = cr.from(User.class);
        Join<User, Role> joinedRole = root.join("role");
        if (onlyActive) {
            cr.select(root).where(cb.equal(root.get("locked"), false));
        } else {
            cr.select(root);
        }
        return em.createQuery(cr).getResultList();
    }

    public void beginTransaction() {
        var transaction = em.getTransaction();
        if (transaction.isActive()) {
            return;
        }
        transaction.begin();
    }

    public void saveContext() {
        var transaction = em.getTransaction();
        if (transaction.isActive()) {
            transaction.commit();
        }
    }

    public void createUser(User user) {
        beginTransaction();
        em.persist(user);
    }

    public void updateUser(User user) {
        beginTransaction();
        em.merge(user);
    }

    public void saveNote(Note note) {
        beginTransaction();
        em.persist(note);
    }


    public void updateNote(Note note) {
        beginTransaction();
        em.merge(note);
    }

    public void saveSubscription(Subscription subscription) {
        beginTransaction();
        em.persist(subscription);
    }

    public void deleteSubscription(Subscription subscription) {
        beginTransaction();
        em.remove(subscription);
    }

    public void deleteNote(Note note) {
        beginTransaction();
        em.remove(note);
    }

    public void saveGrade(Grade grade) {
        beginTransaction();
        em.persist(grade);
    }

    public void updateGrade(Grade grade) {
        beginTransaction();
        em.merge(grade);
    }

    public void saveRole(Role role) {
        beginTransaction();
        em.persist(role);
    }


    public boolean isInRole(String login, RoleEnum role) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cr = cb.createQuery(User.class);
        Root<User> root = cr.from(User.class);
        Join<User, Role> joinedRole = root.join("role");
        cr.select(root).where(cb.equal(cb.lower(root.get("login")), login.toLowerCase())).where(cb.equal(joinedRole.get("name"), role));
        User user = null;
        try {
            user = em.createQuery(cr).getSingleResult();
        } catch (NoResultException e) {
            logger.error("User not found by username {} and role {}", login, role, e);
        }
        return user != null;
    }

    public User getUserByLogin(String login) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cr = cb.createQuery(User.class);
        Root<User> root = cr.from(User.class);

        cr.select(root).where(cb.equal(cb.lower(root.get("login")), login.toLowerCase()));
        User user = null;
        try {
            user = em.createQuery(cr).getSingleResult();
        } catch (NoResultException e) {
            logger.error("User not found by login {}", login, e);
        }
        return user;
    }

    public User getUserById(UUID id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cr = cb.createQuery(User.class);
        Root<User> root = cr.from(User.class);

        cr.select(root).where(cb.equal(root.get("userId"), id));
        User user = null;
        try {
            user = em.createQuery(cr).getSingleResult();
        } catch (NoResultException e) {
            logger.error("User not found by id {}", id, e);
        }
        return user;
    }

    public Note getNoteById(UUID id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cr = cb.createQuery(Note.class);
        Root<Note> root = cr.from(Note.class);

        cr.select(root).where(cb.equal(root.get("noteId"), id));
        Note note = null;
        try {
            note = em.createQuery(cr).getSingleResult();
        } catch (NoResultException e) {
            logger.error("Note not found by id {}", id, e);
        }
        return note;
    }


    public List<Note> getNotesByUserId(UUID id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cr = cb.createQuery(Note.class);
        Root<Note> root = cr.from(Note.class);

        CriteriaQuery<Note> where = cr.select(root).where(
                cb.and(cb.equal(root.get("userId"), id),
                        cb.isNull(root.get("parentNoteId"))));

        return em.createQuery(where).getResultList();
    }


    public List<Subscription> getUserSubscriptions(UUID subscriberId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Subscription> cr = cb.createQuery(Subscription.class);
        Root<Subscription> root = cr.from(Subscription.class);

        CriteriaQuery<Subscription> where = cr.select(root).where(
                cb.equal(root.get("subscriberId"), subscriberId));

        return em.createQuery(where).getResultList();
    }

    public Subscription getSubscriptionById(UUID subscriberId, UUID blogOwnerId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Subscription> cr = cb.createQuery(Subscription.class);
        Root<Subscription> root = cr.from(Subscription.class);

        CriteriaQuery<Subscription> where = cr.select(root).where(
                cb.and(cb.equal(root.get("subscriberId"), subscriberId),
                        cb.equal(root.get("blogOwnerId"), blogOwnerId)));
        Subscription subscription = null;
        try {
            subscription = em.createQuery(where).getSingleResult();
        } catch (NoResultException e) {
            logger.error("Note not found by subscriberId {} blogOwnerId {}", subscriberId, blogOwnerId, e);
        }
        return subscription;
    }

    public Grade getGrade(UUID noteId, UUID userId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Grade> cr = cb.createQuery(Grade.class);
        Root<Grade> root = cr.from(Grade.class);

        CriteriaQuery<Grade> where = cr.select(root).where(
                cb.and(cb.equal(root.get("noteId"), noteId),
                        cb.equal(root.get("userId"), userId)));
        Grade grade = null;
        try {
            grade = em.createQuery(where).getSingleResult();
        } catch (NoResultException e) {
            logger.error("Grade not found by noteId {} userId {}", noteId, userId, e);
        }
        return grade;
    }

    public UUID getRoleId(RoleEnum role) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UUID> cr = cb.createQuery(UUID.class);
        Root<Role> root = cr.from(Role.class);

        cr.select(root.get("roleId")).where(cb.equal(root.get("name"), role));
        UUID id = null;
        try {
            id = em.createQuery(cr).getSingleResult();
        } catch (NoResultException e) {
            logger.error("Role not found by name {}", role, e);
        }
        return id;
    }

    @Override
    public void close() throws Exception {

        if (em.isOpen()) {
            em.close();
        }
        if (factory != null) {
            factory.close();
            factory = null;
        }
    }
}
