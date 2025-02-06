package mc.merge.util

import mc.merge.ModCore.mc
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier

object Versioned {
    fun identifier(value: String): Identifier =
    //? if <1.21 {
        Identifier(value)
    //?} else {
        /*Identifier.of(value)
    *///?}


    fun damageable(item: Item): Boolean =
    //? if <1.20.6 {
        item.isDamageable
    //?} else {
        /*item.defaultStack.isDamageable
    *///?}

    fun tickDelta(): Float =
    //? if <1.21 {
        mc.tickDelta
    //?} else {
        /*mc.renderTickCounter.getTickDelta(false)
    *///?}

    //? if <1.21 {
    fun enchantmentLevel(enchantment: Enchantment, itemStack: ItemStack): Int =
        EnchantmentHelper.getLevel(enchantment, itemStack)
    //?} else {
    /*fun InGame.enchantmentLevel(enchantment: RegistryKey<Enchantment>, itemStack: ItemStack): Int { /^Enchantments.EFFICIENCY^/
        val enchantmentEntry = world.registryManager.get(Enchantments.EFFICIENCY.registryRef).getEntry(enchantment).get()
        return EnchantmentHelper.getLevel(enchantmentEntry, itemStack)
    }
    *///?}

    //? if <1.21 {
    fun enchantmentLevel(enchantment: Enchantment, entity: LivingEntity): Int =
        EnchantmentHelper.getEquipmentLevel(enchantment, entity)
    //?} else {
        /*fun InGame.enchantmentLevel(enchantment: RegistryKey<Enchantment>, entity: LivingEntity): Int {
            val enchantmentEntry = world.registryManager.get(Enchantments.EFFICIENCY.registryRef).getEntry(enchantment).get()
            return EnchantmentHelper.getEquipmentLevel(enchantmentEntry, entity)
        }
    *///?}
}