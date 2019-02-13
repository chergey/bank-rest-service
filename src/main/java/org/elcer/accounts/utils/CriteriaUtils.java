package org.elcer.accounts.utils;

import lombok.experimental.UtilityClass;
import org.elcer.accounts.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.util.function.BiFunction;


@UtilityClass
public class CriteriaUtils {

    @SafeVarargs
    public static <T> TypedQuery<T> createQuery(EntityManager em, Class<T> clazz,
                                                BiFunction<CriteriaBuilder, Root<T>, Expression<Boolean>>... expressions) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<T> q = builder.createQuery(clazz);
        Root<T> root = q.from(clazz);
        CriteriaQuery<T> select = q.select(root);
        for (BiFunction<CriteriaBuilder, Root<T>, Expression<Boolean>> expression : expressions) {
            select.where(expression.apply(builder, root));
        }
        return em.createQuery(q);
    }

}
