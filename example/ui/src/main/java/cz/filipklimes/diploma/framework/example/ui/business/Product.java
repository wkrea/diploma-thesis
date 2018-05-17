package cz.filipklimes.diploma.framework.example.ui.business;

import lombok.Getter;

public class Product
{

    @Getter
    private Integer id;

    @Getter
    private Integer sellPrice;

    @Getter
    private Integer costPrice;

    @Getter
    private String name;

    @Getter
    private String description;

    public Product()
    {
    }

    public Product(final Integer id, final Integer sellPrice, final Integer costPrice, final String name, final String description)
    {
        this.id = id;
        this.sellPrice = sellPrice;
        this.costPrice = costPrice;
        this.name = name;
        this.description = description;
    }

}
