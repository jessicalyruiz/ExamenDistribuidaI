package com.distribuida.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@ApplicationScoped
public class JpaConfig {
    private EntityManagerFactory emf;

    @PostConstruct //indica que el método anotado debe ser ejecutado después de que la instancia de la clase ha sido creada y antes de que sea utilizada.
    public void init() {
        System.out.println("***init");
        emf = Persistence.createEntityManagerFactory("pu-distribuida");
    }

    @Produces //anotación de CDI que indica que el método anotado debe ser utilizado para producir instancias de EntityManager.
    public EntityManager em() { //actúa como un productor de instancias de EntityManager que será inyectado en otras partes de la aplicación.
        System.out.println("***em");
        return emf.createEntityManager();
    }
}

