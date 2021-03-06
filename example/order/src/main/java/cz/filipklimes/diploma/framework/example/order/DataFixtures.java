package cz.filipklimes.diploma.framework.example.order;

import cz.filipklimes.diploma.framework.example.order.business.Address;
import cz.filipklimes.diploma.framework.example.order.business.User;
import cz.filipklimes.diploma.framework.example.order.exception.ProductNotFoundException;
import cz.filipklimes.diploma.framework.example.order.facade.OrderFacade;
import cz.filipklimes.diploma.framework.example.order.facade.ShoppingCartFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataFixtures
{

    private static Logger log = LoggerFactory.getLogger(DataFixtures.class);

    private final OrderFacade orderFacade;
    private final ShoppingCartFacade shoppingCartFacade;

    public DataFixtures(final OrderFacade orderFacade, final ShoppingCartFacade shoppingCartFacade)
    {
        this.orderFacade = orderFacade;
        this.shoppingCartFacade = shoppingCartFacade;
        this.createFixtures();
    }

    private void createFixtures()
    {
        try {
            User user = new User(1, "John Doe", "john.doe@example.com", "CUSTOMER");
            shoppingCartFacade.addProduct(user, 1, BigDecimal.ONE);
            orderFacade.createOrder(
                user,
                new Address("Czech Republic", "Prague", "Karlovo Namesti 5", "15000"),
                new Address("Czech Republic", "Prague", "Karlovo Namesti 5", "15000")
            );

            shoppingCartFacade.addProduct(user, 1, BigDecimal.ONE);

        } catch (ProductNotFoundException e) {
            log.error(String.format("Could not create fixtures: %s", e.getMessage()));
        }
    }

}
