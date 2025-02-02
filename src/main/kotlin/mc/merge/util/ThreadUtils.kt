package mc.merge.util

import com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread
import com.mojang.blaze3d.systems.RenderSystem.recordRenderCall

object ThreadUtils {

    inline fun runOnMainThread(crossinline run: () -> Unit) {
        if (isOnRenderThread()) {
            run()
            return
        }

        recordRenderCall {
            run()
        }
    }
}