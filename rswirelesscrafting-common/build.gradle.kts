plugins {
    id("com.refinedmods.refinedarchitect.common")
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

refinedarchitect {
    common()
}

base {
    archivesName.set("rswirelesscrafting-common")
}

// 与 neoforge 模块保持一致：插件会在求值前用 CI 环境变量重算 version，这里显式覆盖本地版本号。
version = "0.1.0"

val refinedstorageVersion: String by project

dependencies {
    api("com.refinedmods.refinedstorage:refinedstorage-common:${refinedstorageVersion}")
}
