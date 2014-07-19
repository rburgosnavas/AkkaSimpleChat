package com.rburgosnavas.room

import akka.actor.{Actor, ActorSystem, Props}
import com.rburgos.event._

import scala.collection.mutable.ListBuffer

/**
 * Room actor registers and unregisters Clients, receives and broadcasts 
 * messages to Clients
 */
class Room extends Actor {
  // A list to add or remove users.
  var members = new ListBuffer[Login]

  def receive = {
    // Receives a Login and adds it to the list.
    // Prints the number of users connected to the Room, and who they are.
    // Broadcasts the new user in the Room to all but the newly connected user.
    case login @ Login(user, _) =>
      members += login
      println(s"${members.length} connection(s)")
      members foreach printMember

      members filter (_.user != user) foreach {
        _.actor ! Broadcast(user, "<connected>")
      }
      println()
    // Receives a message from a user and Broadcasts it to all other users.
    case send @ Send(user, message) =>
      members filter (_.user != user) foreach {
        _.actor ! Broadcast(user, message)
      }
    // Receives a Logout, removes a user from the list.
    // Prints the user who left the room, the number of connections, and 
    // remaining users.
    // Broadcasts the name of the user who left the room to remaining users.
    case Logout(user, actor) =>
      members -= Login(user, actor)
      println(s"${user} quit...\n")
      println(s"${members.length} connection(s)")
      members foreach {
        (member) => {
          member.actor ! Broadcast(user, "<disconnected>")
          printMember(member)
        }
      }
      println()
  }

  private def printMember(member: Login) = {
    println(s"${member.user} " +
      s"@ ${member.actor.path.address.host.getOrElse("unknown-host")}")
  }
}

object Room extends App {
  // Initialize ActorSystem, Room actor
  val sys = ActorSystem("room-system")
  val room = sys.actorOf(Props[Room], name = "room-actor")
}

