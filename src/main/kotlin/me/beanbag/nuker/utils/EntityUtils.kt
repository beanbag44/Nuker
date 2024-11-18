package me.beanbag.nuker.utils

import net.minecraft.entity.EntityType

object EntityUtils {
    @Suppress("SpellCheckingInspection")
    fun isAttackable(type: EntityType<*>): Boolean {
        return type !== EntityType.AREA_EFFECT_CLOUD
                && type !== EntityType.ARROW
                && type !== EntityType.FALLING_BLOCK
                && type !== EntityType.FIREWORK_ROCKET
                && type !== EntityType.ITEM
                && type !== EntityType.LLAMA_SPIT
                && type !== EntityType.SPECTRAL_ARROW
                && type !== EntityType.ENDER_PEARL
                && type !== EntityType.EXPERIENCE_BOTTLE
                && type !== EntityType.POTION
                && type !== EntityType.TRIDENT
                && type !== EntityType.LIGHTNING_BOLT
                && type !== EntityType.FISHING_BOBBER
                && type !== EntityType.EXPERIENCE_ORB
                && type !== EntityType.EGG
    }
}