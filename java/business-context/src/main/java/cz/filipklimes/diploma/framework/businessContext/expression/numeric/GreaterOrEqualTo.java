package cz.filipklimes.diploma.framework.businessContext.expression.numeric;

import cz.filipklimes.diploma.framework.businessContext.expression.BinaryOperator;
import cz.filipklimes.diploma.framework.businessContext.expression.Expression;
import cz.filipklimes.diploma.framework.businessContext.expression.ExpressionVisitor;
import cz.filipklimes.diploma.framework.businessContext.weaver.BusinessOperationContext;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

public class GreaterOrEqualTo extends BinaryOperator<Boolean, BigDecimal, BigDecimal> implements Serializable
{

    public GreaterOrEqualTo(final Expression<BigDecimal> left, final Expression<BigDecimal> right)
    {
        super(left, right);
    }

    @Override
    public Boolean interpret(final BusinessOperationContext context)
    {
        boolean result = getLeft().interpret(context).compareTo(getRight().interpret(context)) >= 0;
        return result;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return Collections.emptyMap();
    }

    @Override
    public String getName()
    {
        return "numeric-gte";
    }

    @Override
    public void accept(final ExpressionVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toString()
    {
        return String.format("%s >= %s", getLeft(), getRight());
    }

}
