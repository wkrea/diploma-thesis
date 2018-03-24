package cz.filipklimes.diploma.framework.example.order;

import cz.filipklimes.diploma.framework.businessContext.BusinessContext;
import cz.filipklimes.diploma.framework.businessContext.BusinessContextIdentifier;
import cz.filipklimes.diploma.framework.businessContext.BusinessContextRegistry;
import cz.filipklimes.diploma.framework.businessContext.loader.LocalBusinessContextLoader;
import cz.filipklimes.diploma.framework.businessContext.loader.RemoteBusinessContextLoader;
import cz.filipklimes.diploma.framework.businessContext.loader.remote.RemoteLoader;
import cz.filipklimes.diploma.framework.businessContext.loader.remote.RemoteServiceAddress;
import cz.filipklimes.diploma.framework.businessContext.loader.remote.grpc.GrpcRemoteLoader;
import cz.filipklimes.diploma.framework.businessContext.weaver.BusinessContextWeaver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.*;

@SpringBootApplication
public class Application
{

    private static String PRODUCT_HOST = "localhost";
    private static int PRODUCT_PORT = 5552;

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public static BusinessContextRegistry businessContextRegistry()
    {
        Map<String, RemoteLoader> remoteLoaders = new HashMap<>();
        remoteLoaders.put("product", new GrpcRemoteLoader(new RemoteServiceAddress("product", PRODUCT_HOST, PRODUCT_PORT)));

        BusinessContextRegistry registry = BusinessContextRegistry.builder()
            .withLocalLoader(new LocalBusinessContextLoader(){
                @Override
                public Set<BusinessContext> load()
                {
                    return Collections.singleton(
                        BusinessContext.builder()
                            .withIncludedContext(BusinessContextIdentifier.parse("product.listAll"))
                            .withIdentifier(BusinessContextIdentifier.parse("order.addToShoppingCart"))
                            .build()
                    );
                }
            })
            .withRemoteLoader(new RemoteBusinessContextLoader(remoteLoaders))
            .build();

        registry.initialize();

        return registry;
    }

    @Bean
    public static BusinessContextWeaver businessRuleEvaluator(final BusinessContextRegistry businessContextRegistry)
    {
        return new BusinessContextWeaver(businessContextRegistry);
    }

}
