package com.rburgos.event

import akka.actor.ActorRef

/**
 * Events passed between Client and Room
 */
sealed trait Event
case class Login(user: String, actor: ActorRef) extends Event
case class Logout(user: String, actor: ActorRef) extends Event
case class Send(user: String, msg: String) extends Event
case class Broadcast(user: String, msg: String) extends Event
