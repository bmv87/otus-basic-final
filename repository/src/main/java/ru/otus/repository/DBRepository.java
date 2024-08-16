package ru.otus.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.repository.entities.*;
import ru.otus.repository.models.PaginatedResult;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DBRepository implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(DBRepository.class);
    private final EntityManager em;

    public DBRepository() {
        var factory = EntityManagerUtil.getEntityManagerFactory();
        logger.debug(factory.toString());

        em = factory.createEntityManager();
        em.setFlushMode(FlushModeType.COMMIT);
        logger.debug(em.toString());
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
            try {
                em.flush();
                transaction.commit();
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }

    }

    public PaginatedResult<User> getUsers(boolean onlyActive,
                                          UUID excludeUserId,
                                          String username,
                                          GenderEnum gender,
                                          Integer age,
                                          Integer page,
                                          Integer limit) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> dataQuery = cb.createQuery(User.class);
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> dataRoot = dataQuery.from(User.class);
        Root<User> countRoot = countQuery.from(User.class);
        var dataPredicates = new ArrayList<Predicate>();
        var countPredicates = new ArrayList<Predicate>();
        if (excludeUserId != null) {
            dataPredicates.add(cb.notEqual(dataRoot.get("userId"), excludeUserId));
            countPredicates.add(cb.notEqual(countRoot.get("userId"), excludeUserId));
        }
        if (username != null && !username.isBlank()) {
            dataPredicates.add(cb.like(cb.lower(dataRoot.get("username")), "%" + username.toLowerCase() + "%"));
            countPredicates.add(cb.like(cb.lower(countRoot.get("username")), "%" + username.toLowerCase() + "%"));
        }
        if (gender != null) {
            dataPredicates.add(cb.equal(dataRoot.get("gender"), gender));
            countPredicates.add(cb.equal(countRoot.get("gender"), gender));
        }
        if (age != null) {
            dataPredicates.add(cb.equal(dataRoot.get("age"), age));
            countPredicates.add(cb.equal(countRoot.get("age"), age));
        }
        if (onlyActive) {
            dataPredicates.add(cb.equal(dataRoot.get("locked"), false));
            countPredicates.add(cb.equal(countRoot.get("locked"), false));
        }

        countQuery.select(cb.count(countRoot));
        dataQuery.select(dataRoot);
        if (!dataPredicates.isEmpty()) {
            dataQuery.where(cb.and(dataPredicates.toArray(new Predicate[dataPredicates.size()])));
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[countPredicates.size()])));
        }
        Long count = em.createQuery(countQuery).getSingleResult();

        var query = em.createQuery(dataQuery);
        if (limit != null && limit > 0) {
            page = page == null ? 1 : page;
            var offset = (page - 1) * limit;
            query.setFirstResult(offset);
            query.setMaxResults(limit);
        }
        return new PaginatedResult<>(count, em.createQuery(dataQuery).getResultList());
    }

    public <T> void delete(T entity) {
        beginTransaction();
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    public <T> void save(T entity) {
        beginTransaction();
        em.persist(entity);
    }

    public <T> void update(T entity) {
        beginTransaction();
        em.merge(entity);
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

    public List<Note> getNotesByParentId(UUID id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cr = cb.createQuery(Note.class);
        Root<Note> root = cr.from(Note.class);

        CriteriaQuery<Note> where = cr.select(root).where(cb.equal(root.get("parentNoteId"), id));
        return em.createQuery(where).getResultList();
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

    public List<FileInfo> getFilesByUserId(UUID id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<FileInfo> cr = cb.createQuery(FileInfo.class);
        Root<FileInfo> root = cr.from(FileInfo.class);

        CriteriaQuery<FileInfo> where = cr.select(root).where(cb.equal(root.get("userId"), id));

        return em.createQuery(where).getResultList();
    }

    public FileInfo getFileById(UUID id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<FileInfo> cr = cb.createQuery(FileInfo.class);
        Root<FileInfo> root = cr.from(FileInfo.class);

        CriteriaQuery<FileInfo> where = cr.select(root).where(cb.equal(root.get("fileId"), id));

        try {
           return em.createQuery(where).getSingleResult();
        } catch (NoResultException e) {
            logger.error("FileInfo not found by id {}", id, e);
        }
        return null;
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
        try {
            return em.createQuery(where).getSingleResult();
        } catch (NoResultException e) {
            logger.error("Note not found by subscriberId {} blogOwnerId {}", subscriberId, blogOwnerId, e);
        }
        return null;
    }

    public Grade getGrade(UUID noteId, UUID userId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Grade> cr = cb.createQuery(Grade.class);
        Root<Grade> root = cr.from(Grade.class);

        CriteriaQuery<Grade> where = cr.select(root).where(
                cb.and(cb.equal(root.get("noteId"), noteId),
                        cb.equal(root.get("userId"), userId)));
        try {
            return em.createQuery(where).getSingleResult();
        } catch (NoResultException e) {
            logger.error("Grade not found by noteId {} userId {}", noteId, userId, e);
        }
        return null;
    }

    public UUID getRoleId(RoleEnum role) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UUID> cr = cb.createQuery(UUID.class);
        Root<Role> root = cr.from(Role.class);

        cr.select(root.get("roleId")).where(cb.equal(root.get("name"), role));
        try {
            return em.createQuery(cr).getSingleResult();
        } catch (NoResultException e) {
            logger.error("Role not found by name {}", role, e);
        }
        return null;
    }

    public List<User> getUsersByRole(RoleEnum role) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> cr = cb.createQuery(User.class);
        Root<User> root = cr.from(User.class);
        Join<User, Role> joinedRole = root.join("role");
        cr.select(root).where(cb.equal(joinedRole.get("name"), role));
        return em.createQuery(cr).getResultList();
    }

    @Override
    public void close() throws Exception {
        logger.debug(em.toString());

        if (em.isOpen()) {
            em.close();
        }
    }
}
