package ru.otus.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.repository.entities.*;
import ru.otus.repository.models.PaginatedResult;
import ru.otus.repository.specifications.*;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DBContext implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(DBContext.class);
    private final EntityManager em;

    Map<Class<?>, Repository<?>> repos = new HashMap<>();

    public DBContext() {
        var factory = EntityManagerUtil.getEntityManagerFactory();
        logger.debug(factory.toString());

        em = factory.createEntityManager();
        em.setFlushMode(FlushModeType.COMMIT);
        logger.debug(em.toString());
    }


    private <T> Repository<T> getRepository(Class<T> clazz) {
        if (!repos.containsKey(clazz)) {
            var repo = new Repository<T>(clazz, em);
            repos.put(clazz, repo);
            return repo;
        }
        return (Repository<T>) repos.get(clazz);
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


    public void save(User entity) {
        getRepository(User.class).save(entity);
    }


    public void update(User entity) {
        getRepository(User.class).update(entity);
    }


    public void save(Grade entity) {
        getRepository(Grade.class).save(entity);
    }


    public void update(Grade entity) {
        getRepository(Grade.class).update(entity);
    }


    public void delete(Subscription entity) {
        getRepository(Subscription.class).delete(entity);
    }


    public void save(Subscription entity) {
        getRepository(Subscription.class).save(entity);
    }


    public void delete(Note entity) {
        getRepository(Note.class).delete(entity);
    }


    public void save(Note entity) {
        getRepository(Note.class).save(entity);
    }


    public void update(Note entity) {
        getRepository(Note.class).update(entity);
    }


    public void delete(FileInfo entity) {
        getRepository(FileInfo.class).delete(entity);
    }


    public void save(FileInfo entity) {
        getRepository(FileInfo.class).save(entity);
    }


    public Note getNoteById(UUID id) {
        return getRepository(Note.class).getById(id);
    }


    public List<Note> getNotesByUserId(UUID id) {
        return getRepository(Note.class).getFilteredList(NoteSpecifications.getByUserIdSpecification(id));
    }


    public List<FileInfo> getFilesByUserId(UUID id) {
        return getRepository(FileInfo.class).getFilteredList(FileSpecifications.getByUserIdSpecification(id));
    }


    public FileInfo getFileById(UUID id) {
        return getRepository(FileInfo.class).getById(id);
    }


    public Grade getGrade(UUID noteId, UUID userId) {
        try {
            return getRepository(Grade.class).getSingle(GradeSpecifications.getByNoteIdUserIdSpecification(noteId, userId));
        } catch (NoResultException e) {
            logger.error("Grade not found by noteId {} userId {}", noteId, userId, e);
        }
        return null;
    }


    public PaginatedResult<User> getUsers(boolean onlyActive,
                                          UUID excludeUserId,
                                          String username,
                                          GenderEnum gender,
                                          Integer age,
                                          Integer page,
                                          Integer limit) {
        var specification = UserSpecifications.getUsersListSpecification(onlyActive, excludeUserId, username, gender, age);

        return getRepository(User.class).getFilteredAndPaginated(specification, User.Fields.username, page, limit);
    }


    public boolean isInRole(String login, RoleEnum role) {
        return getRepository(User.class).exists(UserSpecifications.getInRoleSpecification(login, role));
    }


    public User getUserByLogin(String login) {
        try {
            return getRepository(User.class).getSingleNativeQuery("SELECT * FROM users WHERE login = ?1", Map.of(1, login));
        } catch (NoResultException e) {
            logger.error("User not found by login {}", login, e);
        }
        return null;
    }


    public User getUserById(UUID id) {
        return getRepository(User.class).getById(id);
    }


    public UUID getRoleId(RoleEnum role) {
        try {
            return getRepository(Role.class).getFieldValue(RoleSpecifications.getByNameSpecification(role), Role.Fields.roleId, UUID.class);
        } catch (NoResultException e) {
            logger.error("Role not found by name {}", role, e);
        }
        return null;
    }


    public List<Subscription> getUserSubscriptions(UUID subscriberId) {
        return getRepository(Subscription.class).getFilteredList(SubscriptionSpecifications.getBySubscriberIdSpecification(subscriberId));
    }


    public Subscription getSubscriptionById(UUID subscriberId, UUID blogOwnerId) {
        return getRepository(Subscription.class).getById(new SubscriptionId(subscriberId, blogOwnerId));
    }


    @Override
    public void close() throws Exception {
        logger.debug(em.toString());
        repos.clear();
        if (em.isOpen()) {
            em.close();
        }
    }
}
