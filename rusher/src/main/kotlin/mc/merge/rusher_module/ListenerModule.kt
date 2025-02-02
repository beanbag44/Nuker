package mc.merge.rusher_module

import mc.merge.event.EventBus
import mc.merge.event.events.RenderEvent
import net.minecraft.client.util.math.MatrixStack
import org.rusherhack.client.api.events.render.EventRender3D
import org.rusherhack.client.api.feature.module.Module
import org.rusherhack.client.api.feature.module.ModuleCategory
import org.rusherhack.core.event.subscribe.Subscribe

@Suppress("unused")
class ListenerModule : Module("Listener Module", ModuleCategory.CLIENT) {
    init {
        this.isHidden = true
    }

    @Subscribe
    fun onRender(event: EventRender3D) {
        EventBus.post(
            RenderEvent.Render3DEvent(
                RusherRenderer3D(
                    event.renderer,
                    event.javaClass.superclass.getMethod("getMatrixStack").invoke(event) as MatrixStack
                )
            )
        )
    }
}