package com.rburgosnavas.client

import java.util.Scanner

import akka.actor.{Props, ActorSystem, Actor}
import com.rburgos.event.{Broadcast, Login, Logout, Send}

class Client extends Actor {
  val remote = context.actorSelection(
    "akka.tcp://room-system@127.0.0.1:5000/user/room-actor")

  def receive = {
    case login @ Login(_, _) =>
      remote ! login
    case send @ Send(_, message) =>
      remote ! send
    case broadcast @ Broadcast(user, message) =>
      print(s"\n${user}: ${message}\n> ")
    case logout @ Logout(_, _) =>
      remote ! logout
      context.stop(self)
  }
}

object Client extends App {
  val user = args(0)
  
  implicit val sys = ActorSystem("client-system")
  val localActor = sys.actorOf(Props[Client], name = "client-actor")
  localActor ! Login(user, localActor)

  val in = new Scanner(System.in)
  var run = true

  while (run) {
    print("> ")
    val message = in.nextLine

    if (message == "/q") {
      localActor ! Logout(user, localActor)
      run = false
    } else {
      localActor ! Send(user, message)
    }
  }

  //  sys.shutdown()
  System.exit(0)
}
