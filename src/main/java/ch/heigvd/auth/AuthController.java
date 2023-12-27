package ch.heigvd.auth;

import ch.heigvd.users.User;
import io.javalin.http.*;

import java.util.concurrent.ConcurrentHashMap;

public class AuthController {

    private final ConcurrentHashMap<Integer, User> users;

    public AuthController(ConcurrentHashMap<Integer, User> users) {
        this.users = users;
    }

    public void login(Context ctx) {
        User loginUser = ctx.bodyValidator(User.class)
                .check(obj -> obj.email != null, "Missing email")
                .check(obj -> obj.password != null, "Missing password")
                .get();

        for (User user : users.values()) {
            if (user.email.equals(loginUser.email) && user.password.equals(loginUser.password)) {
                ctx.cookie("user", user.id.toString());
                ctx.status(HttpStatus.NO_CONTENT);
                return;
            }
        }

        throw new UnauthorizedResponse();
    }

    public void logout(Context ctx) {
        ctx.removeCookie("user");
        ctx.status(HttpStatus.NO_CONTENT);
    }

    public void profile(Context ctx) {
        String userId = ctx.cookie("user");

        if (userId == null) {
            throw new UnauthorizedResponse();
        }

        User user = users.get(Integer.parseInt(userId));

        if (user == null) {
            throw new UnauthorizedResponse();
        }

        ctx.json(user);
    }
}