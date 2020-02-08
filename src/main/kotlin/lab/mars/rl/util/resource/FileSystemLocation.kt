package lab.mars.rl.util.resource

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL

/**
 * A resource loading location that searches somewhere on the classpath
 *
 * @author kevin
 */

/**
 * Create a new resoruce location based on the file system
 *
 * @param root The root of the file system to search
 */
class FileSystemLocation(private val root: File) : ResourceLocation {

    /**
     * @see ResourceLocation.getResource
     */
    override fun getResource(ref: String): URL? {
        return try {
            var file = File(root, ref)
            if (!file.exists()) {
                file = File(ref)
            }
            if (!file.exists()) {
                null
            } else file.toURI().toURL()
        } catch (e: IOException) {
            null
        }
    }

    /**
     * @see ResourceLocation.getResourceAsStream
     */
    override fun getResourceAsStream(ref: String): InputStream? {
        return try {
            var file = File(root, ref)
            if (!file.exists()) {
                file = File(ref)
            }
            FileInputStream(file)
        } catch (e: IOException) {
            null
        }
    }

}