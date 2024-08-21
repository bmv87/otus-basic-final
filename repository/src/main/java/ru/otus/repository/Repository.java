package ru.otus.repository;

import ru.otus.repository.models.PaginatedResult;
import ru.otus.repository.specifications.Specification;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

public class Repository<T> implements AutoCloseable {
    protected final EntityManager em;
    protected final Class<T> clazz;

    public Repository(Class<T> clazz, EntityManager entityManager) {
        this.clazz = clazz;
        em = entityManager;
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

    public PaginatedResult<T> getFilteredAndPaginated(Specification<T> specification,
                                                      String orderField,
                                                      Integer page,
                                                      Integer limit) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> dataQuery = cb.createQuery(clazz);
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> dataRoot = dataQuery.from(clazz);
        Root<T> countRoot = countQuery.from(clazz);
        var dataPredicates = specification.toPredicate(dataRoot, dataQuery, cb);
        var countPredicates = specification.toPredicate(countRoot, countQuery, cb);

        countQuery.select(cb.count(countRoot));
        dataQuery.select(dataRoot);
        if (!dataPredicates.getExpressions().isEmpty()) {
            dataQuery.where(dataPredicates);
            countQuery.where(countPredicates);
        }
        Long count = em.createQuery(countQuery).getSingleResult();
        dataQuery.orderBy(cb.asc(dataRoot.get(orderField)));
        var query = em.createQuery(dataQuery);

        if (limit != null && limit > 0) {
            page = page == null ? 1 : page;
            var offset = (page - 1) * limit;
            query.setFirstResult(offset);
            query.setMaxResults(limit);
        }
        return new PaginatedResult<T>(count, query.getResultList());
    }

    public boolean exists(Specification<T> specification) {
        return count(specification) > 0;
    }

    public Long count(Specification<T> specification) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(clazz);
        var countPredicates = specification.toPredicate(countRoot, countQuery, cb);

        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates);
        return em.createQuery(countQuery).getSingleResult();
    }


    public void delete(T entity) {
        beginTransaction();
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    public void save(T entity) {
        beginTransaction();
        em.persist(entity);
    }

    public void update(T entity) {
        beginTransaction();
        em.merge(entity);
    }

    public T getById(Object id) {
        return em.find(clazz, id);
    }

    public <S> S getFieldValue(Specification<T> specification, String fieldName, Class<S> fieldType) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<S> cr = cb.createQuery(fieldType);
        Root<T> root = cr.from(clazz);

        cr.select(root.get(fieldName)).where(specification.toPredicate(root, cr, cb));

        return em.createQuery(cr).getSingleResult();
    }

    public List<T> getFilteredList(Specification<T> specification) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> dataQuery = cb.createQuery(clazz);
        Root<T> dataRoot = dataQuery.from(clazz);
        var dataPredicates = specification.toPredicate(dataRoot, dataQuery, cb);

        dataQuery.select(dataRoot);
        dataQuery.where(dataPredicates);

        return em.createQuery(dataQuery).getResultList();
    }

    public T getSingle(Specification<T> specification) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> dataQuery = cb.createQuery(clazz);
        Root<T> dataRoot = dataQuery.from(clazz);
        var dataPredicates = specification.toPredicate(dataRoot, dataQuery, cb);

        dataQuery.select(dataRoot);
        dataQuery.where(dataPredicates);
        return em.createQuery(dataQuery).getSingleResult();
    }

    public T getSingleNativeQuery(String queryString, Map<Integer, Object> params) {
        Query query = em.createNativeQuery(queryString, clazz);
        if (params != null && !params.isEmpty()) {
            for (var param : params.entrySet()) {
                query.setParameter(param.getKey(), param.getValue());
            }
        }
        return (T) query.getSingleResult();
    }

    public List<T> getFilteredListNativeQuery(String queryString, Map<Integer, Object> params) {
        Query query = em.createNativeQuery(queryString, clazz);
        if (params != null && !params.isEmpty()) {
            for (var param : params.entrySet()) {
                query.setParameter(param.getKey(), param.getValue());
            }
        }
        return (List<T>) query.getResultList();
    }

    @Override
    public void close() throws Exception {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
