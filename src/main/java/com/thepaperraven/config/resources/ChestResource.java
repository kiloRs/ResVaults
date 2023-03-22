package com.thepaperraven.config.resources;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Material;

public class ChestResource {
    @Getter
    private final Material material;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private int value;

    public ChestResource(Material material){
        this(material,material.name().replace("_"," "));
    }
    public ChestResource(Material material, String name){
        this(material.getKey().getKey(),name);
    }
    public ChestResource(String i){
        this(i,i.replace("_"," "));
    }
    public ChestResource(String i, String name){
        this.material = Material.matchMaterial(i);
        this.name = name;
        this.value = 1;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ChestResource that)) return false;

        return new EqualsBuilder().append(material, that.material).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(material).toHashCode();
    }
}
