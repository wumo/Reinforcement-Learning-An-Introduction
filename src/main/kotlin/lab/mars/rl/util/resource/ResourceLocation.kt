package lab.mars.rl.util.resource

import java.io.InputStream
import java.net.URL

/**
 * A location from which resources can be loaded
 *
 * @author kevin
 */
interface ResourceLocation {
    /**
     * Get a resource as an input stream
     *
     * @param ref The reference to the resource to retrieve
     * @return A stream from which the resource can be read or
     * null if the resource can't be found in this location
     */
    fun getResourceAsStream(ref: String): InputStream?

    /**
     * Get a resource as a URL
     *
     * @param ref The reference to the resource to retrieve
     * @return A URL from which the resource can be read
     */
    fun getResource(ref: String): URL?
}