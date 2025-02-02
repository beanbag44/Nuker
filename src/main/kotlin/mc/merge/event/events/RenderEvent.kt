package mc.merge.event.events

import mc.merge.render.IRenderer2D
import mc.merge.render.IRenderer3D

abstract class RenderEvent : Event {
    class Render2DEvent(val renderer2D: IRenderer2D) : RenderEvent()
    class Render3DEvent(val renderer3D: IRenderer3D) : RenderEvent()
}