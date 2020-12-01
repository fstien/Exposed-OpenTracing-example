package com.github.fstien

import com.github.fstien.exposed.opentracing.NoPII
import com.github.fstien.exposed.opentracing.tracedTransaction
import com.zopa.ktor.opentracing.span
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {

    init {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

        tracedTransaction(contains = NoPII) {
            SchemaUtils.create(Users)
        }
    }

    fun add(user: User) = span("UserRepository.add()") {
        transaction {
            Users.insert {
                it[username] = user.username
                it[age] = user.age
                it[password] = user.password
            }
        }
    }

    fun get(username: String): User? = span("UserRepository.get()") {
        val user = transaction {
            Users.select { Users.username eq username }
        }

        if (user.fetchSize == 0) return null

        val userRow = transaction {
            user.first()
        }

        return User(userRow[Users.username], userRow[Users.age], userRow[Users.password])
    }

    fun delete(username: String) {
        transaction {
            Users.deleteWhere {
                Users.username eq username
            }
        }
    }

}

object Users: IntIdTable() {
    val username = varchar("name", 50)
    val age = integer("age")
    val password = varchar("password", 50)
}

data class User(
    val username: String,
    val age: Int,
    val password: String
)