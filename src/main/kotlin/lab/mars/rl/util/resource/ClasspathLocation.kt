package lab.mars.rl.util.resource

import java.io.InputStream
import java.net.URL

/**
 * A resource location that searches the classpath
 *
 * @author kevin
 */
class ClasspathLocation : ResourceLocation {
    override fun getResource(ref: String): URL {
        val cpRef = ref.replace('\\', '/')
        return ResourceLoader::class.java.classLoader.getResource(cpRef)
    }

    override fun getResourceAsStream(ref: String): InputStream {
        val cpRef = ref.replace('\\', '/')
        return ResourceLoader::class.java.classLoader.getResourceAsStream(cpRef)
    }
}