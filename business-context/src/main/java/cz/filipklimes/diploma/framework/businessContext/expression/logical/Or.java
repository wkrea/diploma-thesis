package cz.filipklimes.diploma.framework.businessContext.expression.logical;

import cz.filipklimes.diploma.framework.businessContext.BusinessContext;
import cz.filipklimes.diploma.framework.businessContext.expression.BinaryOperator;
import cz.filipklimes.diploma.framework.businessContext.expression.Expression;

public final class Or extends BinaryOperator<Boolean, Boolean, Boolean>
{

    public Or(final Expression<Boolean> left, final Expression<Boolean> right)
    {
        super(left, right);
    }

    @Override
    public Boolean interpret(final BusinessContext context)
    {
        return getLeft().interpret(context) || getRight().interpret(context);
    }

}