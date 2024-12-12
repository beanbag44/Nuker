package me.beanbag.nuker.inventory


interface IInventoryResult

class AwaitingCooldown : IInventoryResult
class CantControl : IInventoryResult
class Interacted : IInventoryResult