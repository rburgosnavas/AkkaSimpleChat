package com.rburgosnavas.room

import akka.actor.{Actor, ActorSystem, Props}
import com.rburgos.event._

import scala.collection.mutable.ListBuffer


class Room extends Actor {
  var members = new ListBuffer[Login]

  def receive = {
    case login @ Login(user, _) =>
      members += login
      println(s"${members.length} connection(s)")
      members foreach { println(_) }

      members filter (_.user != user) foreach {
        _.actor ! Broadcast(user, "<connected>")
      }
      println()
    case send @ Send(user, message) =>
      members filter (_.user != user) foreach {
        _.actor ! Broadcast(user, message)
      }
    case Logout(user, actor) =>
      members -= Login(user, actor)
      println(s"${user} quit...\n")
      println(s"${members.length} connection(s)")
      members foreach {
        (member) => {
          member.actor ! Broadcast(user, "<disconnected>")
          println(member)
        }
      }
      println()
  }
}

object Room extends App {
  val sys = ActorSystem("room-system")
  val remoteActor = sys.actorOf(Props[Room], name = "room-actor")
  remoteActor ! "start..."
}

