package org.example;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        // Press Alt+Intro with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        // Press Mayús+F10 or click the green arrow button in the gutter to run the code.
        for (int i = 1; i <= 5; i++) {

            // Press Mayús+F9 to start debugging your code. We have set one breakpoint
            // for you, but you can always add more by pressing Ctrl+F8.
            System.out.println("i = " + i);
        }
    }
}
























/*
****************************buid.gradle.kts






plugins {
    id("java")
    id("application") // crear aplicaciones ejecutables.
    id("io.freefair.lombok") version "8.4" //getters, setters
    id("com.github.johnrengelman.shadow") version "8.1.1" //creación de "fat JARs" (JARs que contienen todas las dependencias)

}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.jboss.weld.se:weld-se-core:5.1.2.Final")//para la inyección de dependencias
    implementation("org.hibernate:hibernate-core:6.4.1.Final")//mapeo objeto-relacional
    implementation("com.h2database:h2:2.2.224") //base de datos embebida

    implementation("com.sparkjava:spark-core:2.9.4")//un marco web ligero
    implementation("com.google.code.gson:gson:2.10.1")//trabajar con JSON

}

tasks.test {
    useJUnitPlatform()
}

sourceSets {//Configura el conjunto de fuentes principal del proyecto y especifica la ubicación del directorio de salida de recursos
    main {
        output.setResourcesDir( file("${buildDir}/classes/java/main") )
    }
}
tasks.jar {//Configura la tarea de construcción del JAR y especifica atributos del manifiesto, como la clase principal y la ruta de clase.
    manifest{
        attributes(
                mapOf("Main-Class" to "com.distribuida.Principal",
                        "Class-Path" to configurations.runtimeClasspath
                                .get()
                                .joinToString(separator = " "){
                                    file->"${file.name}"
                                })
        )
    }
}






****************************META-INF

**************beans.xml

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_2_0.xsd"
       bean-discovery-mode="annotated" version="2.0">

</beans>




******************************* com.distribuida
******************** .config
**JpaConfig

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



**********************************service personaImpl


package com.distribuida.servicios;

import com.distribuida.db.Persona;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
@ApplicationScoped
public class ServicioPersonaImpl implements IServicioPersona{
    @Inject
    EntityManager entityManager;
    @Override
    public void create(Persona persona) {
        var tx=entityManager.getTransaction();

        try {
            tx.begin();
            entityManager.persist(persona);
            tx.commit();
        }
        catch (Exception ex){
            tx.rollback();
        }
    }

    @Override
    public Persona read(Integer id) {
        return entityManager.find(Persona.class, id);
    }

    @Override
    public void update(Persona persona) {
        entityManager.merge(persona);
    }

    @Override
    public void delete(Integer id) {
        entityManager.remove(this.read(id));
    }

    @Override
    public List<Persona> findAll() {
        return entityManager.createQuery("Select p from Persona p").getResultList();
    }

    @Override
    public Persona findByCedula(String cedula) {
        TypedQuery<Persona> myQuery=entityManager.createQuery("Select p from Persona p where p.cedula=:valor", Persona.class);
        myQuery.setParameter("valor", cedula);
        return myQuery.getSingleResult();
    }
}




*********************************principal
package com.distribuida;

import com.distribuida.db.Persona;
import com.distribuida.servicios.IServicioPersona;
import com.google.gson.Gson;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import spark.Request;
import spark.Response;

import java.util.List;

import static spark.Spark.*;

public class Principal {
    static SeContainer container;

    static List<Persona> listarPersonas(Request req, Response resp){
        var servicio=container.select(IServicioPersona.class).get(); //obtener una instancia del bean ed. una instancia de IServicioPersona
        resp.type("application/json");
        return servicio.findAll();
    }

    //buscar por id

    static Persona buscarPersonaById(Request req, Response resp){
        var servicio=container.select(IServicioPersona.class).get();
        resp.type("application/json");

        //recupero el id
        String id=req.params(":id"); //usa el request para obtener el id desde la url

        Persona persona=servicio.read(Integer.valueOf(id));

        if(persona==null){
            // 404 - Persona no encontrada
       halt(404,"Persona no encontrada"); //etener la ejecución del manejo de una solicitud y enviar una respuesta HTTP al cliente indicando que el recurso solicitado no se ha encontrado.
        }
        return persona;

    }
    //update

    static String actualizar(Request req, Response resp){
        var servicio=container.select(IServicioPersona.class).get();
        int id= Integer.parseInt(req.params(":id"));
        //recupero el body
        String body=req.body();
        Gson gson=new Gson();
        Persona p=gson.fromJson(body, Persona.class);
        p.setId(id);
        servicio.update(p);
        resp.status(200);
        return "Persona actualizada correctamente";
    }

    static String insertar(Request req, Response resp){
        var servicio=container.select(IServicioPersona.class).get();

        String body= req.body();
        Gson gson=new Gson();
        Persona p=gson.fromJson(body, Persona.class);
        servicio.create(p);
        resp.status(201);  // Código 201 indica "Created"
        return "Persona insertada exitosamente";
    }

    static String delete(Request req, Response resp){
        var servicio=container.select(IServicioPersona.class).get();

        Integer id = Integer.valueOf(req.params(":id"));
        Persona p=servicio.read(id);
        if(p==null){
            halt(404,"Persona no encontrada");
        }
        servicio.delete(id);
        return "Persona eliminada";
    }

    static Persona buscarCedula(Request req, Response resp){
        var servicio=container.select(IServicioPersona.class).get();
        return servicio.findByCedula(req.params(":cedula"));

    }




    public static void main(String[] args) {
        container= SeContainerInitializer.newInstance().initialize();

        //Obtengo una instancia del Iservicio persona
        IServicioPersona servicio=container.select(IServicioPersona.class).get();
        port(8080);


        // Configuración de rutas para manejar solicitudes HTTP

        Gson gson = new Gson();
        get("/personas", Principal::listarPersonas, gson::toJson); //Configura una ruta para manejar solicitudes HTTP GET a la ruta "/personas".
        get("/personas/:id", Principal::buscarPersonaById, gson::toJson);
        //post("/personas", Principal::insertar);
        post("/personas", Principal::insertar, gson::toJson);
       // delete("/personas/:id", Principal::delete,gson::toJson);
        delete("/personas/:id", Principal::delete, gson::toJson);
        put("/personas/:id",Principal::actualizar,gson::toJson);
        get("/personas/ced/:cedula", Principal::buscarCedula, gson::toJson);
    }



}



*/
 */