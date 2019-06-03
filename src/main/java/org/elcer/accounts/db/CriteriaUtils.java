package org.elcer.accounts.db;

import lombok.experimental.UtilityClass;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.util.function.BiFunction;


@UtilityClass
public class CriteriaUtils {

    @SafeVarargs
    public static <T> TypedQuery<T> createQuery(EntityManager em, Class<T> clazz,
                                                BiFunction<CriteriaBuilder, Root<T>, Expression<Boolean>>... expressions) {
        var builder = em.getCriteriaBuilder();
        var q = builder.createQuery(clazz);
        var root = q.from(clazz);
        var select = q.select(root);
        for (var expression : expressions) {
            select.where(expression.apply(builder, root));
        }
        return em.createQuery(q);
    }

}
