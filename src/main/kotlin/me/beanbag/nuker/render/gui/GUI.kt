package me.beanbag.nuker.render.gui

import me.beanbag.nuker.eventsystem.EventBus
import me.beanbag.nuker.eventsystem.events.RenderEvent
import me.beanbag.nuker.eventsystem.onInGameEvent
import me.beanbag.nuker.handlers.ChatHandler
import org.lwjgl.nuklear.*
import org.lwjgl.system.MemoryUtil


object GUI {
    var nkInitialized = false
    var enabled = true
    lateinit var ctx: NkContext
    private lateinit var nkAllocator: NkAllocator
    private lateinit var nkUserFont: NkUserFont

    init {
        EventBus.onInGameEvent<RenderEvent.Render2DEvent> {
            if (enabled && nkInitialized) {
                drawGUI()
            }
        }
    }

    fun initGUI() {
        ctx = NkContext.create()
        nkAllocator = NkAllocator.create().apply {
            alloc { _, _, size -> MemoryUtil.nmemAlloc(size) }
            free { ptr -> MemoryUtil.nmemFree(ptr) }
        }
        nkUserFont = NkUserFont.create().apply {
            width { _, _, _, len -> 8f * len }
            height(18f)
        }
        nkInitialized = true
        Nuklear.nk_init(ctx, nkAllocator, nkUserFont)
    }

    fun drawGUI() {
        if (Nuklear.nk_begin(
                ctx, "Demo", NkRect.create().set(50f, 50f, 230f, 250f),
                Nuklear.NK_WINDOW_BORDER or Nuklear.NK_WINDOW_MOVABLE or Nuklear.NK_WINDOW_SCALABLE or
                        Nuklear.NK_WINDOW_MINIMIZABLE or Nuklear.NK_WINDOW_TITLE
            )
        ) {
            Nuklear.nk_layout_row_static(ctx, 30f, 80, 1)
            if (Nuklear.nk_button_label(ctx, "Button 1")) {
                ChatHandler.sendChatLine("Button 1 Pressed")
            }

            if (Nuklear.nk_button_label(ctx, "Button 2")) {
                ChatHandler.sendChatLine("Button 2 Pressed")
            }

            Nuklear.nk_layout_row_dynamic(ctx, 30f, 1)
            Nuklear.nk_label(ctx, "Hello, Nuklear!", Nuklear.NK_TEXT_CENTERED)
        }
        Nuklear.nk_end(ctx)
    }
}