package com.epam.esm.repository.impl;

import com.epam.esm.entity.GiftCertificate;
import com.epam.esm.entity.Order;
import com.epam.esm.entity.Tag;
import com.epam.esm.entity.User;
import com.epam.esm.repository.TagRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Objects;

@Repository
@Transactional
public class TagRepositoryImpl implements TagRepository {
    private static final String ORDER_LIST_ATTRIBUTE = "orderList";
    private static final String CERTIFICATE_LIST_ATTRIBUTE = "giftCertificateList";
    private static final String TAG_ATTRIBUTE = "tags";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "nameTag";
    private static final int POSITION_WITH_MAX_VALUE = 1;
    private static final String SQL_FIND_TAG_BY_NAME = "select t.id, t.nameTag from Tag t where t.name = ?";

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<Tag> findAll(int offset, int limit) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tag> tagCriteriaQuery = criteriaBuilder.createQuery(Tag.class);
        Root<Tag> root = tagCriteriaQuery.from(Tag.class);
        tagCriteriaQuery.select(root);
        return entityManager.createQuery(tagCriteriaQuery)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

    }

    @Override
    public Tag findById(long id) {
        return entityManager.find(Tag.class, id);
    }

    @Override
    public List<Tag> findByName(String tagName) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tag> giftCertificateCriteriaQuery = criteriaBuilder.createQuery(Tag.class);
        Root<Tag> root = giftCertificateCriteriaQuery.from(Tag.class);
        Predicate predicate = criteriaBuilder.like(root.get(COLUMN_NAME), tagName);
        giftCertificateCriteriaQuery.select(root).where(predicate);
        return entityManager.createQuery(giftCertificateCriteriaQuery)
                .getResultList();
    }

    @Override
    public Tag create(Tag entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public long delete(long id) {
        entityManager.remove(findById(id));
        return id;
    }

    @Override
    public Long findNumberOfEntities() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        query.select(builder.count(query.from(Tag.class)));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public Tag getMostWidelyUsedUsersTag(long userId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tag> tagQuery = criteriaBuilder.createQuery(Tag.class);
        Root<User> userRoot = tagQuery.from(User.class);

        ListJoin<User, Order> orderList = userRoot.joinList(ORDER_LIST_ATTRIBUTE);
        ListJoin<Order, GiftCertificate> giftList = orderList.joinList(CERTIFICATE_LIST_ATTRIBUTE);
        ListJoin<GiftCertificate, Tag> tagList = giftList.joinList(TAG_ATTRIBUTE);

        Expression orderId = tagList.get("id");
        tagQuery.select(tagList)
                .where(criteriaBuilder.equal(userRoot.get(COLUMN_ID), userId))
                .groupBy(orderId)
                .orderBy(criteriaBuilder.desc(criteriaBuilder.count(orderId)));

        return entityManager.createQuery(tagQuery).setMaxResults(POSITION_WITH_MAX_VALUE).getSingleResult();
    }
}
