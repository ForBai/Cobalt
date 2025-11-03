package org.cobalt.api.module

object ModuleManager {

  private val moduleList = mutableListOf<Module>()

  fun addModules(vararg modules: Module) {
    moduleList.addAll(listOf(*modules))
  }

  fun getModules(): List<Module> {
    return moduleList
  }

}
