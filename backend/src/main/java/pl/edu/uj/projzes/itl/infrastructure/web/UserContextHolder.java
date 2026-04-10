package pl.edu.uj.projzes.itl.infrastructure.web;

import pl.edu.uj.projzes.itl.domain.user.CurrentUser;

public class UserContextHolder {

    private static final ThreadLocal<CurrentUser> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {}

    public static void set(CurrentUser user) {
        CONTEXT.set(user);
    }

    public static CurrentUser get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
