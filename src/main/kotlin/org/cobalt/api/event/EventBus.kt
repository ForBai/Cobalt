package org.cobalt.api.event

import java.io.File
import java.io.IOException
import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.zip.ZipFile
import org.cobalt.api.event.annotation.SubscribeEvent

object EventBus {

  private val listeners = ConcurrentHashMap<Class<*>, List<ListenerData>>()

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

        listeners.compute(eventType) { _, list ->
          val newList = ArrayList(list ?: emptyList())
          newList.add(ListenerData(obj, consumer, priority))
          newList.sort()
          Collections.unmodifiableList(newList)
        }
      }
    }
  }

  @Suppress("UNUSED")
  @JvmStatic
  fun unregister(obj: Any) {
    if (!registered.remove(obj)) return

    listeners.keys.forEach { key ->
      listeners.compute(key) { _, list ->
        val newList = ArrayList(list ?: return@compute null)
        if (newList.removeIf { it.instance === obj }) {
          if (newList.isEmpty()) null else Collections.unmodifiableList(newList)
        } else {
          list
        }
      }
    }
  }

  private val classCache =
    object : ClassValue<List<Class<*>>>() {
      override fun computeValue(type: Class<*>): List<Class<*>> {
        val classes = mutableSetOf<Class<*>>()
        var c: Class<*>? = type
        while (c != null) {
          classes.add(c)
          c.interfaces.forEach { classes.add(it) }
          c = c.superclass
        }
        return classes.toList()
      }
    }

  @JvmStatic
  fun post(event: Event): Event {
    val eventClass = event::class.java

    classCache.get(eventClass).forEach { clazz ->
      listeners[clazz]?.forEach { data -> data.invoker.accept(event) }
    }

    handleDynamic(event)
    return event
  }

  @Suppress("UNCHECKED_CAST")
  private fun createInvoker(instance: Any, method: Method): Consumer<Event> {
    return try {
      method.isAccessible = true

      val lookup = MethodHandles.privateLookupIn(method.declaringClass, MethodHandles.lookup())
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

  @JvmStatic
  fun discoverAndRegister(packageStr: String, excludeFiles: Set<Class<*>> = emptySet()) {
    val path = packageStr.replace('.', '/')
    val resources = Thread.currentThread().contextClassLoader.getResources(path)
    val classes = mutableSetOf<Class<*>>()

    fun findClassesInDir(dir: File, packageName: String) {
      if (!dir.exists()) return

      dir.listFiles()?.forEach { file ->
        if (file.isDirectory) {
          findClassesInDir(file, "$packageName.${file.name}")
        } else if (file.name.endsWith(".class")) {
          val className = "$packageName.${file.name.substring(0, file.name.length - 6)}"
          try {
            classes.add(Class.forName(className))
          } catch (_: ClassNotFoundException) {
          }
        }
      }
    }

    while (resources.hasMoreElements()) {
      val resource = resources.nextElement()
      val filePath = URLDecoder.decode(resource.file, "UTF-8")

      if (resource.protocol == "file") {
        findClassesInDir(File(filePath), packageStr)
      } else if (resource.protocol == "jar") {
        val jarPath = filePath.substring(5, filePath.indexOf("!"))
        try {
          ZipFile(jarPath).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
              val entry = entries.nextElement()
              val name = entry.name

              if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory) {
                val className = name.substring(0, name.length - 6).replace('/', '.')
                try {
                  classes.add(Class.forName(className))
                } catch (_: ClassNotFoundException) {
                }
              }
            }
          }
        } catch (_: IOException) {
        }
      }
    }

    for (clazz in classes) {
      if (clazz in excludeFiles) continue
      if (clazz.declaredMethods.none { it.isAnnotationPresent(SubscribeEvent::class.java) }) {
        continue
      }

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
  ) : Comparable<ListenerData> {
    override fun compareTo(other: ListenerData): Int {
      return other.priority.compareTo(this.priority)
    }
  }

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
