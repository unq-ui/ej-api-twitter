package twitter.api


import io.javalin.Javalin
import twitter.model.Twitter
import twitter.model.NotFound
import twitter.model.UsernameExist
import io.javalin.core.util.RouteOverviewPlugin

fun main(args: Array<String>) {
    val app = Javalin.create {
        it.defaultContentType = "application/json"
        it.registerPlugin(RouteOverviewPlugin("/routes"))
        it.enableCorsForAllOrigins()
    }
        .start(7000)

    // ctx = Context
    // ctx.req -> Request
    // ctx.res -> Response
    app.get("/") { ctx -> ctx.result("Hello World") }

    val twitter = Twitter(mutableListOf())

    // Crud user

    app.get("/users") {
        it.json(twitter.users.map{ UserView(it)})
    }

    app.get("users/:id") {
        it.json(twitter.getUser(it.pathParam("id")))
    }

    app.post("users") {
        val newUser = it.bodyAsClass(UserRegister::class.java)
        try {
            twitter.register(newUser.username, newUser.password, newUser.displayName)
            it.json("ok")
        } catch (exception: UsernameExist) {
            it.status(400)
            it.json(Handler(400, "Bad request", "Username is token"))
        }
    }

    app.put("users/:id") {
        val userId = it.pathParam("id")
        val updateUser = it.bodyAsClass(UserUpdate::class.java)
        twitter.getUser(userId).displayName = updateUser.displayName
        it.json("updated")
    }

    app.exception(NotFound::class.java) { e, ctx ->
        ctx.status(404)
        ctx.json(NotFoundHandler(e.message!!))
    }
}
