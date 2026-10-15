plugins {
    id("com.refinedmods.refinedarchitect.fabric")
}

repositories {
    mavenCentral()
    maven {
        name = "Refined Storage"
        url = uri("https://maven.creeperhost.net")
        content {
            includeGroup("com.refinedmods.refinedstorage")
            includeGroup("teamreborn")
        }
    }
    maven {
        name = "Cloth Config"
        url = uri("https://maven.shedaniel.me/")
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/releases/")
        content {
            includeGroup("com.terraformersmc")
        }
    }
}

// 与 neoforge 模块保持一致：显式覆盖版本号，避免本地回落 0.0.0。
version = "0.3.1-beta.1"

refinedarchitect {
    modId = "rswirelesscrafting"
    fabric()
}

base {
    archivesName.set("rswirelesscrafting-fabric")
}

val refinedstorageVersion: String by project

val commonJava by configurations.existing
val commonResources by configurations.existing

dependencies {
    compileOnly(project(":rswirelesscrafting-common"))
    commonJava(project(path = ":rswirelesscrafting-common", configuration = "commonJava"))
    commonResources(project(path = ":rswirelesscrafting-common", configuration = "commonResources"))
    // 1.21.1 的 refinedstorage-fabric jar 是 intermediary 映射，必须用 modApi 让 Loom remap 到
    // named 映射；同时把 cloth/energy/modmenu 显式提升为 modApi，避免它们作为普通传递依赖不被 remap。
    modApi("com.refinedmods.refinedstorage:refinedstorage-fabric:${refinedstorageVersion}") {
        exclude(group = "me.shedaniel.cloth")
        exclude(group = "teamreborn")
        exclude(group = "com.terraformersmc")
    }
    modApi("me.shedaniel.cloth:cloth-config-fabric:15.0.140") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modApi("teamreborn:energy:4.1.0") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modApi("com.terraformersmc:modmenu:11.0.3")
}
