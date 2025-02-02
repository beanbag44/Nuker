package mc.merge.module.modules.nuker.enumsettings

enum class RenderType {
    None,
    Both,
    Fill,
    Line;

    fun enabled() =
        this != None
}