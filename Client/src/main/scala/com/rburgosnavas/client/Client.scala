package com.rburgosnavas.client

import java.util.Scanner

import akka.actor.{Props, ActorSystem, Actor}
import com.rburgos.event.{Broadcast, Login, Logout, Send}

/**
 * Client actor used to connect to the Room actor.
 */
class Client extends Actor {
  // Construct a Room actor from the given path.
  // See Room/src/main/resources/application.conf.
  val room = context.actorSelection(
    "akka.tcp://room-system@127.0.0.1:5000/user/room-actor")

  def receive = {
    // Client gets this as soon as Client app starts.
    // Sends Login to Room to be registered.
    case login @ Login(_, _) =>
      room ! login
    // Client gets Send messages from the console by a user.
    // Sends the message to the Room to be broadcasted.
    case send @ Send(_, message) =>
      room ! send
    // Received from a Broadcast by the Room.
    // Simply prints the user and its message.
    case broadcast @ Broadcast(user, message) =>
      print(s"\n${user}: ${message}\n> ")
    // Client gets this when a user enters "/q".
    // Sends Logout to Room to become unregistered.
    // Stops Client actor.
    case logout @ Logout(_, _) =>
      room ! logout
      context.stop(self)
  }
}

object Client extends App {
  val user = {
    if (args.length > 0) args(0)
    else s"(guest-${System.currentTimeMillis})"
  }

  // Initialize ActorSystem and Client actor
  implicit val sys = ActorSystem("client-system")
  val client = sys.actorOf(Props[Client], name = "client-actor")

  // User log in
  client ! Login(user, client)

  val in = new Scanner(System.in)
  var run = true

  // Chat loop
  while (run) {
    print("> ")
    val message = in.nextLine

    // If user enters "/q", send Logout message to Client and stop loop...
    // else send message.
    if (message == "/q") {
      client ! Logout(user, client)
      run = false
    } else {
      client ! Send(user, message)
    }
  }

  //  sys.shutdown()
  System.exit(0)
}
