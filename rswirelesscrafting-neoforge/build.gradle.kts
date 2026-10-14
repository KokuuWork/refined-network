plugins {
    id("com.refinedmods.refinedarchitect.neoforge")
}

repositories {
    mavenCentral()
    maven {
        name = "NeoForged Mirror"
        url = uri("https://mirrors.huaweicloud.com/neoforged/releases/")
    }
    maven {
        name = "Refined Storage"
        url = uri("https://maven.creeperhost.net")
        content {
            includeGroup("com.refinedmods.refinedstorage")
        }
    }
}

// RefinedArchitect 会在项目求值时用 CI 环境变量（GITHUB_SHA / RELEASE_VERSION）计算 version，
// 本地无 CI 时回落为 0.0.0；且 mods.toml 的 ${version} 在扩展注册时就被捕获。
// 因此这里必须在 refinedarchitect { } 之前显式覆盖版本号。
version = "0.1.2"

refinedarchitect {
    modId = "rswirelesscrafting"
    neoForge()
}

base {
    archivesName.set("rswirelesscrafting-neoforge")
}

val refinedstorageVersion: String by project

val commonJava by configurations.existing
val commonResources by configurations.existing

dependencies {
    compileOnly(project(":rswirelesscrafting-common"))
    commonJava(project(path = ":rswirelesscrafting-common", configuration = "commonJava"))
    commonResources(project(path = ":rswirelesscrafting-common", configuration = "commonResources"))
    api("com.refinedmods.refinedstorage:refinedstorage-neoforge:${refinedstorageVersion}")
}
