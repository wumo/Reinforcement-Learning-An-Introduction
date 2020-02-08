package lab.mars.rl.util.resource

import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.*

/**
 * A simple wrapper around resource loading should anyone decide to change
 * their minds how this is meant to work in the future.
 *
 * @author Kevin Glass
 */
object ResourceLoader {
    /** The list of locations to be searched  */
    private val locations: ArrayList<ResourceLocation> = ArrayList()

    /**
     * Add a location that will be searched for resources
     *
     * @param location The location that will be searched for resoruces
     */
    fun addResourceLocation(location: ResourceLocation) {
        locations.add(location)
    }

    /**
     * Remove a location that will be no longer be searched for resources
     *
     * @param location The location that will be removed from the search list
     */
    fun removeResourceLocation(location: ResourceLocation) {
        locations.remove(location)
    }

    /**
     * Remove all the locations, no resources will be found until
     * new locations have been added
     */
    fun removeAllResourceLocations() {
        locations.clear()
    }

    /**
     * Get a resource
     *
     * @param ref The reference to the resource to retrieve
     * @return A stream from which the resource can be read
     */
    fun getResourceAsStream(ref: String): InputStream {
        var `in`: InputStream? = null
        for (i in locations.indices) {
            val location = locations[i]
            `in` = location.getResourceAsStream(ref)
            if (`in` != null) {
                break
            }
        }
        if (`in` == null) {
            throw RuntimeException("Resource not found: $ref")
        }
        return BufferedInputStream(`in`)
    }

    /**
     * Check if a resource is available from any given resource loader
     *
     * @param ref A reference to the resource that should be checked
     * @return True if the resource can be located
     */
    fun resourceExists(ref: String): Boolean {
        var url: URL? = null
        for (i in locations.indices) {
            val location = locations[i]
            url = location.getResource(ref)
            if (url != null) {
                return true
            }
        }
        return false
    }

    /**
     * Get a resource as a URL
     *
     * @param ref The reference to the resource to retrieve
     * @return A URL from which the resource can be read
     */
    fun getResource(ref: String): URL {
        var url: URL? = null
        for (i in locations.indices) {
            val location = locations[i]
            url = location.getResource(ref)
            if (url != null) {
                break
            }
        }
        if (url == null) {
            throw RuntimeException("Resource not found: $ref")
        }
        return url
    }

    init {
        locations.add(ClasspathLocation())
        locations.add(FileSystemLocation(File(".")))
    }
}