package cz.filipklimes.diploma.framework.businessContext.expression;

import cz.filipklimes.diploma.framework.businessContext.weaver.BusinessOperationContext;
import cz.filipklimes.diploma.framework.businessContext.exception.BadObjectPropertyTypeException;
import cz.filipklimes.diploma.framework.businessContext.exception.UndefinedObjectPropertyException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

public class ObjectPropertyReference<T> implements Terminal<T>, Serializable
{

    private final String objectName;
    private final String propertyName;
    private final ExpressionType type;

    public ObjectPropertyReference(final String objectName, final String propertyName, final ExpressionType type)
    {
        this.objectName = Objects.requireNonNull(objectName);
        this.propertyName = Objects.requireNonNull(propertyName);
        this.type = Objects.requireNonNull(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T interpret(final BusinessOperationContext context)
    {
        Object object = context.getInputParameter(objectName);

        Method getter = Arrays.stream(object.getClass().getDeclaredMethods())
            .filter(method -> method.getName().toLowerCase().equals("get" + propertyName.toLowerCase())
                || method.getName().toLowerCase().equals("is" + propertyName.toLowerCase()))
            .findAny()
            .orElseThrow(() -> new UndefinedObjectPropertyException(objectName, propertyName));

        try {
            Object value = getter.invoke(object);
            Class<?> returnType = getter.getReturnType();

            // Numbers shenanigans (we <3 java)
            if (returnType.equals(Integer.class)) {
                returnType = BigDecimal.class;
                value = BigDecimal.valueOf(Integer.class.cast(value));
            } else if (returnType.equals(Long.class)) {
                returnType = BigDecimal.class;
                value = BigDecimal.valueOf(Long.class.cast(value));
            } else if (returnType.equals(Float.class)) {
                returnType = BigDecimal.class;
                value = BigDecimal.valueOf(Float.class.cast(value));
            } else if (returnType.equals(Double.class)) {
                returnType = BigDecimal.class;
                value = BigDecimal.valueOf(Double.class.cast(value));
            }

            if (!returnType.equals(type.getUnderlyingClass())) {
                throw new BadObjectPropertyTypeException(objectName, propertyName, type.getUnderlyingClass());
            }

            return (T) type.getUnderlyingClass().cast(value);

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not access object property", e);
        }
    }

    @Override
    public Map<String, String> getProperties()
    {
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("objectName", objectName);
        propertyMap.put("propertyName", propertyName);
        propertyMap.put("type", type.getName());
        return propertyMap;
    }

    @Override
    public String getName()
    {
        return "object-property-reference";
    }

    @Override
    public void accept(final ExpressionVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toString()
    {
        return String.format("$%s.%s", objectName, propertyName);
    }

}
