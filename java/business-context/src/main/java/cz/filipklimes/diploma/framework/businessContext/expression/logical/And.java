package cz.filipklimes.diploma.framework.businessContext.expression.logical;

import cz.filipklimes.diploma.framework.businessContext.weaver.BusinessOperationContext;
import cz.filipklimes.diploma.framework.businessContext.expression.BinaryOperator;
import cz.filipklimes.diploma.framework.businessContext.expression.Expression;

import java.util.*;

public final class And extends BinaryOperator<Boolean, Boolean, Boolean>
{

    public And(final Expression<Boolean> left, final Expression<Boolean> right)
    {
        super(left, right);
    }

    @Override
    public Boolean interpret(final BusinessOperationContext context)
    {
        return getLeft().interpret(context) && getRight().interpret(context);
    }

    @Override
    public Map<String, String> getProperties()
    {
        return Collections.emptyMap();
    }

    @Override
    public String getName()
    {
        return "logical-and";
    }

    @Override
    public String toString()
    {
        return String.format("%s and %s", getLeft(), getRight());
    }

}