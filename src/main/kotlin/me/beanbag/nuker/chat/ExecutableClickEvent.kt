package me.beanbag.nuker.chat

import net.minecraft.text.ClickEvent

open class ExecutableClickEvent(val onClick: () -> Unit) : ClickEvent(Action.RUN_COMMAND, "Will not be used")