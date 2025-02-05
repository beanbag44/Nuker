plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.20.4" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) { 
    group = "project"
    ofTask("build")
}
//Convenience task for faster builds when working with just rusher
stonecutter registerChiseled tasks.register("rusherBuild", stonecutter.chiseled) {
    nodes { it.branch.id.contains("rusher") }
    group = "project"
    ofTask("build")
}
