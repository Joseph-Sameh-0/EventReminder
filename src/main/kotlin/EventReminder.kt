package org.example

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Event(
    val title: String,
    val date: LocalDateTime,
    val description: String? = null
)

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

class EventManager {

    private val events = mutableListOf<Event>()

    fun addEvent(event: Event?) {
        if (event == null) {
            println("Cannot add a null event.")
            return
        }

        events.add(event)
        println("Event '${event.title}' on ${event.date.format(formatter)} added.")
    }

    fun startReminders() {
        val reminderThread = Thread {
            while (true) {
                Thread.sleep(1000)
                val now = LocalDateTime.now()
                val iterator = events.iterator()
                while (iterator.hasNext()) {
                    val event = iterator.next()
                    try {
                        if (!event.date.isAfter(now)) {
                            println("\n\u001B[33mReminder: ${event.title} at ${event.date.format(formatter)}" + (event.description?.let { ": $it" } ?: "") + "\u001B[0m")
                            iterator.remove()
                        }
                    } catch (_: Exception) {}
                }
                if (events.isEmpty()) continue
            }
        }
        reminderThread.isDaemon = true
        reminderThread.start()
    }
}

fun greetUser(name: String? = null) {
    val greeting = when {
        name.isNullOrBlank() -> "Hello! Welcome to your Event Reminder Assistant."
        else -> "Hello, ${name.trim().replaceFirstChar { it.uppercaseChar() }}! Let's manage your events."
    }
    println(greeting)
}

fun safeRun(action: () -> Unit) {
    try {
        action()
    } catch (e: Exception) {
        println("Oops! Something went wrong: ${e.message}")
    }
}

fun main() {
    val manager = EventManager()

    print("Enter your name: ")
    val userName = readlnOrNull()?.trim()
    greetUser(userName)
    manager.startReminders()

    while (true) {
        print("\nEnter event title (or type 'exit' to quit): ")
        val title = readlnOrNull()?.trim()
        if (title.equals("exit", ignoreCase = true)) break

        var dateTimeString: String?
        lateinit var dateTime: LocalDateTime
        while (true) {
            println("The time now is ${LocalDateTime.now().format(formatter)}.")
            print("Enter event date and time (YYYY-MM-DD HH:mm): ")
            dateTimeString = readlnOrNull()?.trim()
            if (dateTimeString.isNullOrBlank()) {
                println("Date and time cannot be empty. Please try again.")
                continue
            }
            try {
                dateTime = LocalDateTime.parse(dateTimeString, formatter)
                break
            } catch (_: Exception) {
                println("Invalid date format. Please use YYYY-MM-DD HH:mm.")
            }
        }

        print("Enter event description (optional, press Enter to skip): ")
        val description = readlnOrNull()?.trim()
        val desc = if (description.isNullOrBlank()) null else description

        safeRun {
            manager.addEvent(Event(title ?: "", dateTime, desc))
        }
    }
}