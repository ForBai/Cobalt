package org.cobalt.api.util.render

import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ShaderProgram(
    vertexPath: String,
    fragmentPath: String
) {
    val programId: Int
    private val uniformLocations = mutableMapOf<String, Int>()
    
    init {
        val vertexSource = loadResource(vertexPath)
        val fragmentSource = loadResource(fragmentPath)
        
        val vertexShader = compileShader(GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentSource)
        
        programId = glCreateProgram()
        glAttachShader(programId, vertexShader)
        glAttachShader(programId, fragmentShader)
        glLinkProgram(programId)
        
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            val log = glGetProgramInfoLog(programId)
            throw RuntimeException("Shader program linking failed: $log")
        }
        
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
    }
    
    fun use() = glUseProgram(programId)
    fun unbind() = glUseProgram(0)
    
    fun setUniform(name: String, value: Float) {
        glUniform1f(getUniformLocation(name), value)
    }
    
    fun setUniform(name: String, x: Float, y: Float) {
        glUniform2f(getUniformLocation(name), x, y)
    }
    
    fun delete() {
        glDeleteProgram(programId)
    }
    
    private fun getUniformLocation(name: String): Int {
        return uniformLocations.getOrPut(name) {
            glGetUniformLocation(programId, name)
        }
    }
    
    private fun compileShader(type: Int, source: String): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, source)
        glCompileShader(shader)
        
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            val log = glGetShaderInfoLog(shader)
            val typeName = if (type == GL_VERTEX_SHADER) "vertex" else "fragment"
            throw RuntimeException("$typeName shader compilation failed: $log")
        }
        return shader
    }
    
    private fun loadResource(path: String): String {
        return this::class.java.getResourceAsStream(path)
            ?.bufferedReader()
            ?.readText()
            ?: throw IllegalArgumentException("Shader resource not found: $path")
    }
}

object FullScreenQuad {
    private var vao = -1
    private var vbo = -1
    private var initialized = false
    
    private val VERTICES = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )
    
    fun init() {
        if (initialized) return
        
        vao = glGenVertexArrays()
        vbo = glGenBuffers()
        
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        
        val buffer = ByteBuffer.allocateDirect(VERTICES.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(VERTICES)
            .flip()
        
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
        glEnableVertexAttribArray(0)
        
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
        
        initialized = true
    }
    
    fun render() {
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        glBindVertexArray(0)
    }
    
    fun cleanup() {
        if (initialized) {
            glDeleteBuffers(vbo)
            glDeleteVertexArrays(vao)
            initialized = false
        }
    }
}
