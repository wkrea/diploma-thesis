package cz.filipklimes.diploma.framework.businessContext.loader.remote.grpc;

import cz.filipklimes.diploma.framework.businessContext.BusinessContext;
import cz.filipklimes.diploma.framework.businessContext.BusinessContextIdentifier;
import cz.filipklimes.diploma.framework.businessContext.PostCondition;
import cz.filipklimes.diploma.framework.businessContext.PostConditionType;
import cz.filipklimes.diploma.framework.businessContext.Precondition;
import cz.filipklimes.diploma.framework.businessContext.expression.Constant;
import cz.filipklimes.diploma.framework.businessContext.expression.Expression;
import cz.filipklimes.diploma.framework.businessContext.expression.ExpressionType;
import cz.filipklimes.diploma.framework.businessContext.expression.FunctionCall;
import cz.filipklimes.diploma.framework.businessContext.expression.IsNotNull;
import cz.filipklimes.diploma.framework.businessContext.expression.ObjectPropertyAssignment;
import cz.filipklimes.diploma.framework.businessContext.expression.ObjectPropertyReference;
import cz.filipklimes.diploma.framework.businessContext.expression.VariableAssignment;
import cz.filipklimes.diploma.framework.businessContext.expression.VariableReference;
import cz.filipklimes.diploma.framework.businessContext.expression.logical.And;
import cz.filipklimes.diploma.framework.businessContext.expression.logical.Equals;
import cz.filipklimes.diploma.framework.businessContext.expression.logical.Negate;
import cz.filipklimes.diploma.framework.businessContext.expression.logical.Or;
import cz.filipklimes.diploma.framework.businessContext.expression.numeric.Add;
import cz.filipklimes.diploma.framework.businessContext.expression.numeric.Divide;
import cz.filipklimes.diploma.framework.businessContext.expression.numeric.GreaterOrEqualTo;
import cz.filipklimes.diploma.framework.businessContext.expression.numeric.GreaterThan;
import cz.filipklimes.diploma.framework.businessContext.expression.numeric.LessOrEqualTo;
import cz.filipklimes.diploma.framework.businessContext.expression.numeric.LessThan;
import cz.filipklimes.diploma.framework.businessContext.expression.numeric.Multiply;
import cz.filipklimes.diploma.framework.businessContext.expression.numeric.Subtract;
import cz.filipklimes.diploma.framework.businessContext.loader.remote.RemoteServiceAddress;
import cz.filipklimes.diploma.framework.businessContext.provider.server.grpc.BusinessContextProtos.BusinessContextMessage;
import cz.filipklimes.diploma.framework.businessContext.provider.server.grpc.BusinessContextProtos.BusinessContextRequestMessage;
import cz.filipklimes.diploma.framework.businessContext.provider.server.grpc.BusinessContextProtos.ExpressionMessage;
import cz.filipklimes.diploma.framework.businessContext.provider.server.grpc.BusinessContextProtos.ExpressionPropertyMessage;
import cz.filipklimes.diploma.framework.businessContext.provider.server.grpc.BusinessContextProtos.PostConditionMessage;
import cz.filipklimes.diploma.framework.businessContext.provider.server.grpc.BusinessContextProtos.PostConditionTypeMessage;
import cz.filipklimes.diploma.framework.businessContext.provider.server.grpc.BusinessContextProtos.PreconditionMessage;
import cz.filipklimes.diploma.framework.businessContext.provider.server.grpc.BusinessContextServerGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class GrpcBusinessContextClient
{

    private static final Logger logger = Logger.getLogger(GrpcBusinessContextClient.class.getName());

    private final ManagedChannel channel;
    private final BusinessContextServerGrpc.BusinessContextServerBlockingStub blockingStub;

    GrpcBusinessContextClient(final RemoteServiceAddress address)
    {
        this.channel = ManagedChannelBuilder.forAddress(address.getHost(), address.getPort())
            .usePlaintext(true)
            .build();
        this.blockingStub = BusinessContextServerGrpc.newBlockingStub(channel);
    }

    void shutdown() throws InterruptedException
    {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Receives business contexts from gRPC business context server.
     *
     * @param identifiers
     * @return Set of business rules.
     */
    public Set<BusinessContext> receiveContexts(final Set<BusinessContextIdentifier> identifiers)
    {
        try {
            return blockingStub.fetchContexts(buildRequest(identifiers))
                .getContextsList().stream()
                .map(this::buildBusinessContext)
                .collect(Collectors.toSet());

        } catch (StatusRuntimeException e) {
            logger.severe(e.getMessage());
            return Collections.emptySet();
        }
    }

    private BusinessContextRequestMessage buildRequest(final Set<BusinessContextIdentifier> identifiers)
    {
        return BusinessContextRequestMessage.newBuilder()
            .addAllRequiredContexts(identifiers.stream()
                .map(BusinessContextIdentifier::toString)
                .collect(Collectors.toSet()))
            .build();
    }

    private BusinessContext buildBusinessContext(final BusinessContextMessage businessContextMessage)
    {
        return new BusinessContext(
            new BusinessContextIdentifier(businessContextMessage.getPrefix(), businessContextMessage.getName()),
            businessContextMessage.getIncludedContextsList().stream().map(BusinessContextIdentifier::parse).collect(Collectors.toSet()),
            businessContextMessage.getPreconditionsList().stream().map(this::buildPrecondition).collect(Collectors.toSet()),
            businessContextMessage.getPostConditionsList().stream().map(this::buildPostCondition).collect(Collectors.toSet())
        );
    }

    private Precondition buildPrecondition(final PreconditionMessage message)
    {
        return Precondition.builder()
            .withName(message.getName())
            .withCondition(buildExpression(message.getCondition()))
            .build();
    }

    private PostCondition buildPostCondition(final PostConditionMessage message)
    {
        return PostCondition.builder()
            .withName(message.getName())
            .withType(convertType(message.getType()))
            .withReferenceName(message.getReferenceName())
            .withCondition(buildExpression(message.getCondition()))
            .build();
    }

    private PostConditionType convertType(final PostConditionTypeMessage type)
    {
        switch (type) {
            case FILTER_OBJECT_FIELD:
                return PostConditionType.FILTER_OBJECT_FIELD;
            case FILTER_LIST_OF_OBJECTS:
                return PostConditionType.FILTER_LIST_OF_OBJECTS;
            case FILTER_LIST_OF_OBJECTS_FIELD:
                return PostConditionType.FILTER_LIST_OF_OBJECTS_FIELD;
            case UNKNOWN:
            default:
                throw new RuntimeException("Unknown business rule type");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Expression<T> buildExpression(final ExpressionMessage message)
    {
        switch (message.getName()) {
            case "logical-and":
                return (Expression<T>) new And(buildExpression(message.getArguments(0)), buildExpression(message.getArguments(1)));
            case "logical-equals":
                return (Expression<T>) new Equals(buildExpression(message.getArguments(0)), buildExpression(message.getArguments(1)));
            case "logical-negate":
                return (Expression<T>) new Negate(buildExpression(message.getArguments(0)));
            case "logical-or":
                return (Expression<T>) new Or(buildExpression(message.getArguments(0)), buildExpression(message.getArguments(1)));
            case "numeric-add":
                return (Expression<T>) new Add(buildExpression(message.getArguments(0)), buildExpression(message.getArguments(1)));
            case "numeric-divide":
                return (Expression<T>) new Divide(buildExpression(message.getArguments(0)), buildExpression(message.getArguments(1)));
            case "numeric-gte":
                return (Expression<T>) new GreaterOrEqualTo(buildExpression(message.getArguments(0)), buildExpression(message.getArguments(1)));
            case "numeric-gt":
                return (Expression<T>) new GreaterThan(buildExpression(message.getArguments(0)), buildExpression(message.getArguments(1)));
            case "numeric-lte":
                return (Expression<T>) new LessOrEqualTo(buildExpression(message.getArguments(0)), buildExpression(message.getArguments(1)));
            case "numeric-lt":
                return (Expression<T>) new LessThan(buildExpression(message.getArguments(0)), buildExpression(message.getArguments(1)));
            case "numeric-multiply":
                return (Expression<T>) new Multiply(buildExpression(message.getArguments(0)), buildExpression(message.getArguments(1)));
            case "numeric-subtract":
                return (Expression<T>) new Subtract(buildExpression(message.getArguments(0)), buildExpression(message.getArguments(1)));
            case "constant":
                ExpressionType type = ExpressionType.of(findPropertyByName("type", message));
                return (Expression<T>) new Constant<>(type.deserialize(findPropertyByName("value", message)), type);
            case "function-call":
                return (Expression<T>) new FunctionCall<>(
                    findPropertyByName("methodName", message),
                    ExpressionType.of(findPropertyByName("type", message)),
                    (Expression<?>[]) message.getArgumentsList().stream().map(this::buildExpression).toArray()
                );
            case "is-not-null":
                return (Expression<T>) new IsNotNull<>(buildExpression(message.getArguments(0)));
            case "object-property-assignment":
                return (Expression<T>) new ObjectPropertyAssignment<>(
                    findPropertyByName("objectName", message),
                    findPropertyByName("propertyName", message),
                    buildExpression(message.getArguments(0))
                );
            case "object-property-reference":
                return (Expression<T>) new ObjectPropertyReference<>(
                    findPropertyByName("objectName", message),
                    findPropertyByName("propertyName", message),
                    ExpressionType.of(findPropertyByName("type", message))
                );
            case "variable-assignment":
                return (Expression<T>) new VariableAssignment<>(
                    findPropertyByName("name", message),
                    buildExpression(message.getArguments(0))
                );
            case "variable-reference":
                return (Expression<T>) new VariableReference<>(
                    findPropertyByName("name", message),
                    ExpressionType.of(findPropertyByName("type", message))
                );
            default:
                throw new RuntimeException(String.format("Unknown expression: %s", message.getName()));
        }
    }

    private String findPropertyByName(final String name, final ExpressionMessage message)
    {
        for (ExpressionPropertyMessage expressionProperty : message.getPropertiesList()) {
            if (expressionProperty.getKey().equals(name)) {
                return expressionProperty.getValue();
            }
        }
        return null;
    }

}
