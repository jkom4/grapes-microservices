package grapes.microservices

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}