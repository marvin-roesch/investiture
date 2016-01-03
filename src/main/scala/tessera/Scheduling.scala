package tessera

import net.minecraft.world.World
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.{ClientTickEvent, Phase}
import net.minecraftforge.fml.relauncher.Side

import scala.collection.mutable

/**
 * Scheduling
 *
 * @author PaleoCrafter
 */
trait Scheduling {

  object scheduler extends Scheduler

  class Scheduler {
    private val clientTasks = mutable.Buffer.empty[Task]
    private val serverTasks = mutable.Buffer.empty[Task]

    def schedule(task: => Unit, delay: Long)(implicit world: World): Unit =
      schedule(task, delay, if(world.isRemote) Side.CLIENT else Side.SERVER)

    def schedule(task: => Unit, delay: Long, side: Side): Unit =
      side match {
        case Side.CLIENT => clientTasks += new Task(task, delay)
        case Side.SERVER => serverTasks += new Task(task, delay)
      }

    private class Task(f: => Unit, delay: Long) {
      private[this] var counter = delay

      def tick(): Boolean = {
        if (counter <= 0) {
          f
          true
        } else {
          counter -= 1
          false
        }
      }
    }

    private def update(tasks: mutable.Buffer[Task]): Unit =
      tasks --= tasks.filter(_.tick())

    object client {
      def apply(task: => Unit, delay: Long): Unit = schedule(task, delay, Side.CLIENT)
    }

    object server {
      def apply(task: => Unit, delay: Long): Unit = schedule(task, delay, Side.SERVER)
    }

    private[tessera] object events {
      @SubscribeEvent
      def onClientTick(event: ClientTickEvent): Unit = {
        if (event.phase == Phase.END)
          update(clientTasks)
      }

      @SubscribeEvent
      def onServerTick(event: ClientTickEvent): Unit = {
        if (event.phase == Phase.END)
          update(serverTasks)
      }
    }
  }

}
