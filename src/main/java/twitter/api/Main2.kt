package twitter.api

import io.javalin.Javalin
import twitter.model.Twitter
import twitter.model.NotFound
import twitter.model.UsernameExist
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.util.RouteOverviewPlugin

fun main() {
    val app = Javalin.create {
        it.defaultContentType = "application/json"
        it.registerPlugin(RouteOverviewPlugin("/routes"))
        it.enableCorsForAllOrigins()
    }
    app.start(7000)

    val twitter = Twitter(mutableListOf())

    app.routes {
        path("users") {
            get { it.json(twitter.users) }
            post {
                val newUser = it.bodyAsClass(UserRegister::class.java)
                try {
                    twitter.register(newUser.username, newUser.password, newUser.displayName)
                    it.json("ok")
                } catch (exception: UsernameExist) {
                    it.status(400)
                    it.json(Handler(400, "Bad request", "Username is token"))
                }
            }
            path(":id") {
                get { it.json(twitter.getUser(it.pathParam("id"))) }
                put {
                    val userId = it.pathParam("id")
                    val updateUser = it.bodyAsClass(UserUpdate::class.java)
                    twitter.getUser(userId).displayName = updateUser.displayName
                    it.json("updated")
                }
            }
        }
    }

    app.exception(NotFound::class.java) { e, ctx ->
        ctx.status(404)
        ctx.json(NotFoundHandler(e.message!!))
    }
}
