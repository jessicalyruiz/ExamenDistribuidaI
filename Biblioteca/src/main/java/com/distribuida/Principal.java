package com.distribuida;

import com.distribuida.db.Book;
import com.distribuida.service.LibroService;
import com.google.gson.Gson;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import spark.Request;
import spark.Response;

import java.util.List;

import static spark.Spark.*;

public class Principal {
    static SeContainer container;

    static List<Book> listarBooks(Request req, Response resp){
        var servicio=container.select(LibroService.class).get(); //obtener una instancia del bean ed. una instancia de LibroService
        resp.type("application/json");
        return servicio.findAll();
    }

    //buscar por id

    static Book buscarBookById(Request req, Response resp){
        var servicio=container.select(LibroService.class).get();
        resp.type("application/json");

        //recupero el id
        String id=req.params(":id"); //usa el request para obtener el id desde la url

        Book book=servicio.read(Integer.valueOf(id));

        if(book==null){
            // 404 - Book no encontrada
            halt(404,"Book no encontrada"); //etener la ejecución del manejo de una solicitud y enviar una respuesta HTTP al cliente indicando que el recurso solicitado no se ha encontrado.
        }
        return book;

    }
    //update

    static String actualizar(Request req, Response resp){
        var servicio=container.select(LibroService.class).get();
        int id= Integer.parseInt(req.params(":id"));
        //recupero el body
        String body=req.body();
        Gson gson=new Gson();
        Book p=gson.fromJson(body, Book.class);
        p.setId(id);
        servicio.update(p);
        resp.status(200);
        return "Book actualizada correctamente";
    }

    static String insertar(Request req, Response resp){
        var servicio=container.select(LibroService.class).get();

        String body= req.body();
        Gson gson=new Gson();
        Book p=gson.fromJson(body, Book.class);
        servicio.create(p);
        resp.status(201);  // Código 201 indica "Created"
        return "Book insertada exitosamente";
    }

    static String delete(Request req, Response resp){
        var servicio=container.select(LibroService.class).get();

        Integer id = Integer.valueOf(req.params(":id"));
        Book p=servicio.read(id);
        if(p==null){
            halt(404,"Book no encontrada");
        }
        servicio.delete(id);
        return "Book eliminada";
    }

    static Book buscarISBN(Request req, Response resp){
        var servicio=container.select(LibroService.class).get();
        return servicio.findByISBN(req.params(":isbn"));

    }




    public static void main(String[] args) {
        container= SeContainerInitializer.newInstance().initialize();

        //Obtengo una instancia del Iservicio book
        LibroService servicio=container.select(LibroService.class).get();
        port(8080);


        // Configuración de rutas para manejar solicitudes HTTP

        Gson gson = new Gson();
        get("/books", Principal::listarBooks, gson::toJson); //Configura una ruta para manejar solicitudes HTTP GET a la ruta "/books".
        get("/books/:id", Principal::buscarBookById, gson::toJson);
        post("/books", Principal::insertar, gson::toJson);
        //delete("/books/:id", Principal::delete, gson::toJson);
        put("/books/:id",Principal::actualizar,gson::toJson);
        get("/books/ced/:isbn", Principal::buscarISBN, gson::toJson);
    }

}
