package twitter.model

class NotFound(message: String): Exception(message) {}

class UsernameExist(): Exception("Username is used") {}