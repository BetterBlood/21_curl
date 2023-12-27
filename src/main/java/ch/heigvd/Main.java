package ch.heigvd;

import ch.heigvd.auth.AuthController;
import ch.heigvd.users.User;
import ch.heigvd.users.UsersController;

import io.javalin.Javalin;
import io.javalin.http.*;

import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static final int PORT = 8080;

    public static void main(String[] args) {
        Javalin app = Javalin.create();

        app.get("/", ctx ->
                ctx
                        .result("Hello, world from a GET request method with a `HttpStatus.OK` response status!")
        );
        app.post("/", ctx ->
                ctx
                        .result("Hello, world from a POST request method with a `HttpStatus.CREATED` response status!")
                        .status(HttpStatus.CREATED)
        );
        app.patch("/", ctx ->
                ctx
                        .result("Hello, world from a PATCH request method with a `HttpStatus.OK` response status!")
                        .status(HttpStatus.OK)
        );
        app.delete("/", ctx ->
                ctx
                        .result("Hello, world from a DELETE request method with a `HttpStatus.NO_CONTENT` response status!")
                        .status(HttpStatus.NO_CONTENT)
        );

        app.get("/path-parameter-demo/{path-parameter}", ctx -> {
            String pathParameter = ctx.pathParam("path-parameter");

            ctx.result("You just called `/path-parameter-demo` with path parameter '" + pathParameter + "'!");
        });

        app.get("/query-parameters-demo", ctx -> {
            String firstName = ctx.queryParam("firstName");
            String lastName = ctx.queryParam("lastName");

            if (firstName == null || lastName == null) {
                throw new BadRequestResponse();
            }

            ctx.result("Hello, " + firstName + " " + lastName + "!");
        });

        app.post("/body-demo", ctx -> {
            String data = ctx.body();

            ctx.result("You just called `/body-demo` with data '" + data + "'!");
        });

        app.get("/content-negotiation-demo", ctx -> {
            String acceptHeader = ctx.header("Accept");

            if (acceptHeader == null) {
                throw new BadRequestResponse();
            }

            if (acceptHeader.contains("text/html")) {
                ctx.contentType("text/html");
                ctx.result("<h1>Hello, world!</h1>");
            } else if (acceptHeader.contains("text/plain")) {
                ctx.contentType("text/plain");
                ctx.result("Hello, world!");
            } else {
                throw new NotAcceptableResponse();
            }
        });

        app.get("/cookie-demo", ctx -> {
            String cookie = ctx.cookie("cookie");

            if (cookie == null) {
                ctx.cookie("cookie", "cookieValue");

                ctx.result("You just called `/cookie-demo` without a cookie. A cookie is now set!");
            } else {
                ctx.result("You just called `/cookie-demo` with cookie '" + cookie + "'!");
            }
        });



        // This will serve as our database
        ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();

        // Controllers
        AuthController authController = new AuthController(users);
        UsersController usersController = new UsersController(users);

        // Auth routes
        app.post("/login", authController::login);
        app.post("/logout", authController::logout);
        app.get("/profile", authController::profile);

        // Users routes
        app.post("/users", usersController::create);
        app.get("/users", usersController::getMany);
        app.get("/users/{id}", usersController::getOne);
        app.put("/users/{id}", usersController::update);
        app.delete("/users/{id}", usersController::delete);

        app.start(PORT);
    }
}