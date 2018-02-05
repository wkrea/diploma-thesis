package cz.filipklimes.diploma.framework.businessContext.expression;

import lombok.Getter;

import java.util.*;

public abstract class BinaryOperator<T, L, R> implements Expression<T>
{

    @Getter
    private final Expression<L> left;

    @Getter
    private final Expression<R> right;

    public BinaryOperator(final Expression<L> left, final Expression<R> right)
    {
        this.left = left;
        this.right = right;
    }

    @Override
    public Collection<Expression<?>> getArguments()
    {
        return Arrays.asList(left, right);
    }

}
