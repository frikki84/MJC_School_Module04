package com.epam.esm.utils;

import com.epam.esm.entity.GiftCertificate;

import com.epam.esm.entity.SearchGiftCertificateParameter;
import com.epam.esm.entity.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
public class GiftCertificateCriteriaBuilder {
    public static final String REGEX = "%";
    public static final String PARAMETER_NAME = "name";
    public static final String PARAMETER_DESCRIPTION = "description";
    public static final String PARAMETER_TAG_NAMES = "tags";
    public static final String PARAMETER_SORTBY_NAME = "name";
    public static final String PARAMETER_SORTBY_CREATE_DATE = "createDate";
    public static final String TAG_LIST_IN_GIFTCERTIFICATE = "tags";
    public static final String QUERY_SELECT_BY_TAG_NAME = "select tag from Tag tag where tag.nameTag in (:" + PARAMETER_TAG_NAMES + ")";


    @Autowired
    private final EntityManager entityManager;

    public GiftCertificateCriteriaBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public CriteriaQuery<GiftCertificate> buildQuery(SearchGiftCertificateParameter parameter) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GiftCertificate> giftCertificateCriteriaQuery = criteriaBuilder.createQuery(GiftCertificate.class);
        Root<GiftCertificate> root = giftCertificateCriteriaQuery.from(GiftCertificate.class);

        List<Predicate> predicateList = new ArrayList<>();

        String parameterName = parameter.getName();

        if (Objects.nonNull(parameterName)) {
            String pattern = REGEX + parameterName + REGEX;
            Predicate predicate = criteriaBuilder.like(root.get(PARAMETER_NAME), pattern);
            predicateList.add(predicate);
        }

        String parameterDescription = parameter.getDescription();
        if (Objects.nonNull(parameterDescription)) {
            String pattern = REGEX + parameterDescription + REGEX;
            Predicate predicate = criteriaBuilder.like(root.get(PARAMETER_DESCRIPTION), pattern);
            predicateList.add(predicate);
        }

        List<String> tagNames = parameter.getTags();

        if (Objects.nonNull(tagNames)) {
            List<String> distinctTagList = tagNames.stream().distinct().collect(Collectors.toList());
            List<Tag> tags = entityManager.createQuery(QUERY_SELECT_BY_TAG_NAME, Tag.class).setParameter(PARAMETER_TAG_NAMES, distinctTagList).getResultList();
            if (tags.size() != distinctTagList.size()) {
                return giftCertificateCriteriaQuery;
            }
            tags.forEach(tag -> predicateList.add(criteriaBuilder.isMember(tag, root.get(TAG_LIST_IN_GIFTCERTIFICATE))));

        }
        giftCertificateCriteriaQuery.select(root).where(predicateList.toArray(new Predicate[0]));

        SortParameter sortParameter = parameter.getSortBy();

        String sortValue=null;

        if (Objects.nonNull(sortParameter)) {
            switch (sortParameter) {
                case NAME:
                    sortValue = PARAMETER_SORTBY_NAME;
                    break;
                case CREATE_DATE:
                    sortValue = PARAMETER_SORTBY_CREATE_DATE;
                    break;
            }
        }

        OrderType orderType = parameter.getOrder();
        Order order = null;
        if (Objects.nonNull(orderType)) {
            switch (orderType) {
                case ASC:
                    order = criteriaBuilder.asc(root.get(sortValue));
                    break;
                case DESC:
                    order = criteriaBuilder.desc(root.get(sortValue));
                    break;
            }

            giftCertificateCriteriaQuery.orderBy(order);
        }

        return giftCertificateCriteriaQuery;

    }
}
