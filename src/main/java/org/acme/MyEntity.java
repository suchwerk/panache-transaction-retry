package org.acme;

import java.time.LocalDateTime;

import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class MyEntity extends PanacheEntity {

    public LocalDateTime time = LocalDateTime.now();

}
