package akka.dispatch.verification

import akka.actor.{ ActorCell, ActorRef, Props }
import akka.dispatch.{ Envelope }

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.Semaphore



abstract class Event

case class MsgEvent(sender: String, receiver: String, msg: Any, 
    var id: Integer = IDGenerator.get()) extends Event

case class SpawnEvent(parent: String,
    props: Props, name: String, actor: ActorRef, 
    id: Integer = IDGenerator.get()) extends Event




object IDGenerator {
  var obj:Instrumenter = null
  var uniqueId = new AtomicInteger

  def get() : Integer = {
    return uniqueId.incrementAndGet()
  }
}

    
trait TellEnqueue {
  def tell()
  def enqueue()
  def reset()
  def await ()
}

class TellEnqueueBusyWait extends TellEnqueue {
  
  var enqueue_count = new AtomicInteger
  var tell_count = new AtomicInteger
  
  def tell() {
    tell_count.incrementAndGet()
  }
  
  def enqueue() {
    enqueue_count.incrementAndGet()
  }
  
  def reset() {
    tell_count.set(0)
    enqueue_count.set(0)
  }

  def await () {
    while (tell_count.get != enqueue_count.get) {
      
      //println(tell_count.get + " " + enqueue_count.get)
    }
  }
  
}
    

class TellEnqueueSemaphore extends Semaphore(1) with TellEnqueue {
  
  var enqueue_count = new AtomicInteger
  var tell_count = new AtomicInteger
  
  def tell() {
    tell_count.incrementAndGet()
    reducePermits(1)
  }

  def enqueue() {
    enqueue_count.incrementAndGet()
    release()
  }
  
  def reset() {
    tell_count.set(0)
    enqueue_count.set(0)
  }
  
  def await() {
    acquire
    release
  }
}
