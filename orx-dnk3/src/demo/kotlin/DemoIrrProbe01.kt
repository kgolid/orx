import kotlinx.coroutines.yield
import org.openrndr.KEY_ARROW_LEFT
import org.openrndr.KEY_ARROW_RIGHT
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extensions.SingleScreenshot
import org.openrndr.extra.dnk3.*
import org.openrndr.extra.dnk3.features.addIrradianceSH
import org.openrndr.extra.dnk3.gltf.buildSceneNodes
import org.openrndr.extra.dnk3.gltf.loadGltfFromFile
import org.openrndr.extra.dnk3.materials.IrradianceDebugMaterial
import org.openrndr.extras.camera.Orbital
import org.openrndr.extras.meshgenerators.boxMesh
import org.openrndr.extras.meshgenerators.sphereMesh
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.launch
import org.openrndr.math.*
import org.openrndr.math.transforms.transform
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        width = 1280
        height = 720
        //multisample = WindowMultisample.SampleCount(8)
    }

    program {
        if (System.getProperty("takeScreenshot") == "true") {
            extend(SingleScreenshot()) {
                this.outputFile = System.getProperty("screenshotPath")
            }
        }

        var layerIndex = 0
        keyboard.keyDown.listen {
            if (it.key == KEY_ARROW_RIGHT) {
                layerIndex++
            }
            if (it.key == KEY_ARROW_LEFT) {
                layerIndex--
            }
        }

        val gltf = loadGltfFromFile(File("demo-data/town-irr.glb"))
        val scene = Scene(SceneNode())

        val probeBox = sphereMesh(16,16, 0.1)
        val probeGeometry = Geometry(listOf(probeBox), null, DrawPrimitive.TRIANGLES, 0, probeBox.vertexCount)

        scene.addIrradianceSH(20, 20, 20, 2.5)

        val sceneData = gltf.buildSceneNodes()
        scene.root.children.addAll(sceneData.scenes.first())

        // -- create a renderer
        val renderer = dryRenderer()
        val orb = extend(Orbital()) {
            camera.setView(Vector3(-0.49, -0.24, 0.20), Spherical(26.56, 90.0, 6.533), 40.0)
        }



        println("scene hash is: ${scene.hashCode()}")

        renderer.draw(drawer, scene)

        val dynNode = SceneNode()

        val dynMaterial = PBRMaterial()

        val boxM = boxMesh(0.1, 0.1, 0.1)
                    val dynPrimitive = MeshPrimitive(probeGeometry, dynMaterial)
                    val dynMesh = Mesh(listOf(dynPrimitive))
                    dynNode.entities.add(dynMesh)

        dynNode.entities.add(dynMesh)

        scene.dispatcher.launch {
            while(true) {
                dynNode.transform = transform {
                    translate(cos(seconds)*0.5, 0.5, sin(seconds)*0.5)
                    scale(2.0)
                }
                yield()
            }
        }
        scene.root.children.add(dynNode)

        extend {
            drawer.clear(ColorRGBa.BLACK)
            renderer.draw(drawer, scene)
            drawer.defaults()
        }
    }
}