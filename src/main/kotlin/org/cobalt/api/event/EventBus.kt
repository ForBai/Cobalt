package org.cobalt.api.event

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer
import org.cobalt.api.event.annotation.SubscribeEvent
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder

object EventBus {

  private val listeners = ConcurrentHashMap<Class<*>, CopyOnWriteArrayList<ListenerData>>()

  private val registered = ConcurrentHashMap.newKeySet<Any>()
  private val dynamicRunnable = ConcurrentHashMap<Class<out Event>, MutableList<Runnable>>()

  @JvmStatic
  fun register(obj: Any) {
    if (!registered.add(obj)) return

    obj::class.java.declaredMethods.forEach { method ->
      if (method.isAnnotationPresent(SubscribeEvent::class.java)) {
        val params = method.parameterTypes
        require(params.size == 1 && Event::class.java.isAssignableFrom(params[0])) {
          "Invalid Method"
        }

        method.isAccessible = true
        val priority = method.getAnnotation(SubscribeEvent::class.java).priority
        val eventType = params[0]

        val consumer = createInvoker(obj, method)

        listeners
          .computeIfAbsent(eventType) { CopyOnWriteArrayList() }
          .add(ListenerData(obj, consumer, priority, method))

        listeners[eventType]?.sort()
      }
    }
  }

  @Suppress("UNUSED")
  @JvmStatic
  fun unregister(obj: Any) {
    if (!registered.remove(obj)) return

    listeners.values.forEach { list -> list.removeIf { it.instance === obj } }
  }

  @JvmStatic
  fun post(event: Event): Event {
    val eventClass = event::class.java

    listeners[eventClass]?.forEach { data ->
      try {
        data.invoker.accept(event)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    handleDynamic(event)
    return event
  }

  @Suppress("UNCHECKED_CAST")
  private fun createInvoker(instance: Any, method: Method): Consumer<Event> {
    return try {
      method.isAccessible = true
      val lookup = MethodHandles.lookup()
      val methodHandle = lookup.unreflect(method)
      val boundHandle = methodHandle.bindTo(instance)

      val callSite =
        LambdaMetafactory.metafactory(
          lookup,
          "accept",
          MethodType.methodType(Consumer::class.java),
          MethodType.methodType(Void.TYPE, Any::class.java),
          boundHandle,
          MethodType.methodType(Void.TYPE, method.parameterTypes[0])
        )

      callSite.target.invokeExact() as Consumer<Event>
    } catch (e: Throwable) {
      Consumer { evt -> method.invoke(instance, evt) }
    }
  }

  /**
   * Registers all functions with the @SubscribeEvent annotation in the given package.
   *
   * @param packageStr The package to scan for @SubscribeEvent annotated functions.
   * @param excludeFiles A set of classes to exclude from registration.
   *
   * @author oblongboot
   */
  @JvmStatic
  fun discoverAndRegister(packageStr: String, excludeFiles: Set<Class<*>> = emptySet()) {
    val reflections =
      Reflections(
        ConfigurationBuilder()
          .forPackages(packageStr)
          .setScanners(Scanners.MethodsAnnotated)
      )

    val methods = reflections.getMethodsAnnotatedWith(SubscribeEvent::class.java)
    val seen = mutableSetOf<Class<*>>()

    for (method in methods) {
      val clazz = method.declaringClass
      if (!seen.add(clazz) || clazz in excludeFiles) continue

      try {
        val instance =
          when {
            clazz.declaredFields.any { it.name == "INSTANCE" } -> {
              clazz.getDeclaredField("INSTANCE").apply { trySetAccessible() }.get(null)
            }

            else -> {
              val constructor = clazz.getDeclaredConstructor()
              constructor.trySetAccessible()
              constructor.newInstance()
            }
          }

        register(instance)
      } catch (_: Exception) {
      }
    }
  }

  private data class ListenerData(
    val instance: Any,
    val invoker: Consumer<Event>,
    val priority: Int,
    val method: Method,
  ) : Comparable<ListenerData> {
    override fun compareTo(other: ListenerData): Int {
      return other.priority.compareTo(this.priority)
    }
  }

  /**
   * Registers a function to be called when an event is posted, alternative to using the
   * @SubscribeEvent annotation.
   *
   * @param eventClass The event to listen for.
   * @param runnable The function to call when the event is posted.
   *
   * @author oblongboot
   */
  @JvmStatic
  fun registerEvent(eventClass: Class<out Event>, runnable: Runnable) {
    dynamicRunnable.computeIfAbsent(eventClass) { mutableListOf() }.add(runnable)
  }

  @JvmStatic
  fun handleDynamic(event: Event) {
    dynamicRunnable.filter { (clazz, _) -> clazz.isAssignableFrom(event::class.java) }.forEach { (_, listeners) ->
      listeners.forEach { it.run() }
    }
  }
}
