import net.neoforged.moddevgradle.internal.RunGameTask
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.attributes.Attribute
import org.slf4j.event.Level
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.replace


plugins {
    id("java")
    id("java-library")
    id("idea")

    id("localRuntime")

    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.moddev)
}

val modId = Constants.Mod.ID
val mcVersion: String = libs.versions.minecraft.get()
val forgeVersion: String = libs.versions.forge.get()
val kffVersion: String = libs.versions.kotlinForForge.get()

val jdkVersion = Constants.Dev.JDK_VERSION
val jvmVendor = Constants.Dev.JVM_VENDOR


val exportMixin = true
val loadAddons = true


base {
    archivesName = "${rootProject.name}-$mcVersion"
    version = Constants.Mod.VERSION
    group = Constants.Mod.GROUP
}

repositories {
    mavenLocal()
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
    maven {
        name = "Mekanism / JEI"
        url = uri("https://modmaven.dev/")
    }
    maven {
        name = "Curse Maven"
        url = uri("https://cursemaven.com")
    }
    maven {
        name = "R2"
        url = uri("https://maven.lapis256.dev")
    }
    mavenCentral()
}

// 【合并改动】单一元数据任务：不再生成独立的 core 版 mods.toml
val generateModMetadata by tasks.registering(ProcessResources::class)

val coreApiSourceSet: SourceSet = sourceSets.create("core.api", Action {})

val mainApiSourceSet: SourceSet = sourceSets.create("main.api", Action {
    compileClasspath += coreApiSourceSet.output
    runtimeClasspath += coreApiSourceSet.output
})

val coreSourceSet: SourceSet = sourceSets.create("core", Action {
    compileClasspath += coreApiSourceSet.output
    runtimeClasspath += coreApiSourceSet.output

    resources {
        exclude("**/.cache")
    }
})

val mainSourceSet: SourceSet = sourceSets.getByName("main") {
    compileClasspath += mainApiSourceSet.output + coreApiSourceSet.output + coreSourceSet.output
    runtimeClasspath += mainApiSourceSet.output + coreApiSourceSet.output + coreSourceSet.output

    resources {
        srcDirs(
            "src/generated/resources",
            generateModMetadata.get().outputs.files
        )
        exclude("**/.cache")
    }
}

val dataSourceSet: SourceSet = sourceSets.create("data", Action {
    compileClasspath += coreSourceSet.output + mainSourceSet.compileClasspath + mainSourceSet.output
    runtimeClasspath += coreSourceSet.output + mainSourceSet.runtimeClasspath + mainSourceSet.output
})

java {
    registerFeature("coreApi") {
        usingSourceSet(coreApiSourceSet)
    }
    registerFeature("mainApi") {
        usingSourceSet(mainApiSourceSet)
    }
    registerFeature("core") {
        usingSourceSet(coreSourceSet)
    }
}

mixin {
    add(mainSourceSet, "${modId}.refmap.json")
    add(coreSourceSet, "${modId}_core.refmap.json")

    config("${modId}.mixins.json")
    config("${modId}_core.mixins.json")
}

dependencies {
    run {
        val mainApiCompileOnly by configurations.getting

        mainApiCompileOnly(variantOf(libs.mekanism, "api"))
    }

    run {
        val coreCompileOnly by configurations.getting

        coreCompileOnly(libs.mekanism)
        coreCompileOnly(libs.kotlinForForge)
        coreCompileOnly(libs.easyNestConfig)
        coreCompileOnly(libs.mixinExtrasCommon)
        coreCompileOnly(libs.mixinExtrasForge)

        val coreAnnotationProcessor by configurations.getting

        coreAnnotationProcessor(variantOf(libs.mixin, "processor"))
        coreAnnotationProcessor(libs.mixinExtrasCommon)
    }

    run {
        val coreApiCompileOnly by configurations.getting {
            extendsFrom(configurations.getByName("modCompileOnly"))
        }

        coreApiCompileOnly(libs.kotlinForForge)
        coreApiCompileOnly(variantOf(libs.mekanism, "all"))
    }

    // 【合并改动】core / core.api 源码集并入主 mod：原先内嵌在独立 Core jar 里的两个依赖
    // （MixinExtras slim、EasyNestConfig）改为内嵌进唯一的主 jar，不再存在只为 core 产物服务的 jarJar 配置。
    run {
        val jarJar by configurations.getting

        jarJar(variantOf(libs.mixinExtrasForge, "slim")) {
            version {
                strictly("[$this,)")
                prefer(this.toString())
            }
        }

        jarJar(libs.easyNestConfig)
    }

    modImplementation(libs.kotlinForForge)
    modImplementation(libs.mekanism)

    modRuntimeOnly(variantOf(libs.mekanism, "generators"))

    modCompileOnly(variantOf(libs.mekanism, "generators"))
    modCompileOnly(libs.mekanismExtras)
    modCompileOnly(libs.evolvedMekanism)
    modCompileOnly(libs.mekanismElements)
    modCompileOnly(libs.evolvedMekanismExtras)
    modCompileOnly(libs.mekanismMoreMachines)

    modRuntimeOnly(libs.jei)

    if (loadAddons) {
        modRuntimeOnly(libs.mekanismExtras)
        modRuntimeOnly(libs.igleelib)
        modRuntimeOnly(libs.evolvedMekanism)
        modRuntimeOnly(libs.mekanismElements)
        modRuntimeOnly(libs.evolvedMekanismExtras)
        modRuntimeOnly(libs.mekanismMoreMachines)
    }

    modImplementation(libs.easyNestConfig)

    annotationProcessor(variantOf(libs.mixin, "processor"))
    annotationProcessor(libs.mixinExtrasCommon)
    compileOnly(libs.mixinExtrasCommon)
    implementation(libs.mixinExtrasForge)
}

legacyForge {
    version = "$mcVersion-$forgeVersion"

    addModdingDependenciesTo(coreApiSourceSet)
    addModdingDependenciesTo(coreSourceSet)
    addModdingDependenciesTo(mainApiSourceSet)
    addModdingDependenciesTo(dataSourceSet)

    validateAccessTransformers = true

    accessTransformers {
        val atFile = rootProject.file("src/core/resources/META-INF/accesstransformer.cfg")
        if (atFile.exists()) {
            from(atFile)
            publish(atFile)
        }
    }

    parchment {
        mappingsVersion = libs.versions.parchmentmc.get()
        minecraftVersion = mcVersion
    }

    runs {
        create("client", Action {
            client()
            gameDirectory.set(rootProject.file("run"))
            systemProperty("forge.enabledGameTestNamespaces", modId)
            jvmArgument("-Dmixin.debug=true")
            jvmArgument("-Dmixin.debug.export=$exportMixin")
            jvmArgument("-XX:+AllowEnhancedClassRedefinition")
        })

        create("server", Action {
            server()
            gameDirectory.set(rootProject.file("run-server"))
            programArgument("--nogui")
            systemProperty("forge.enabledGameTestNamespaces", modId)
            jvmArgument("-Dmixin.debug=true")
            jvmArgument("-Dmixin.debug.export=$exportMixin")
            jvmArgument("-XX:+AllowEnhancedClassRedefinition")
        })

        create("data", Action {
            data()
            sourceSet = dataSourceSet
            gameDirectory.set(rootProject.file("run-data"))
            programArguments.addAll(
                "--mod",
                modId,
                "--all",
                "--output",
                file("src/generated/resources/").absolutePath,
                "--existing",
                file("src/main/resources/").absolutePath
            )
        })

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")

            logLevel = Level.DEBUG
        }
    }

    mods {
        // 【合并改动】core / core.api 两个源码集并入主 mod：单一 mod id，不再注册独立的 ${modId}_core mod。
        create(modId, Action {
            sourceSet(mainSourceSet)
            sourceSet(mainApiSourceSet)
            sourceSet(dataSourceSet)
            sourceSet(coreApiSourceSet)
            sourceSet(coreSourceSet)
        })
    }

    ideSyncTask(generateModMetadata)
}

fun setupMetaDataTask(modId: String, modName: String, task: TaskProvider<ProcessResources>, deps: List<ModDep>) {
    task {
        val replaceProperties = mutableMapOf(
            "version" to version,
            "group" to project.group,
            "minecraft_version" to mcVersion,
            "mod_loader" to "javafml",
            "mod_loader_version_range" to "[${extractVersionSegments(forgeVersion)},)",
            "mod_name" to modName,
            "mod_author" to Constants.Mod.AUTHOR,
            "mod_id" to modId,
            "license" to Constants.Mod.LICENSE,
            "description" to Constants.Mod.DESCRIPTION,
            "display_url" to Constants.Mod.REPOSITORY_URL,
            "issue_tracker_url" to Constants.Mod.ISSUE_TRACKER_URL,
            "dependencies" to buildDeps(*deps.toTypedArray(), modId = modId),
        )

        inputs.properties(replaceProperties)
        filter<ReplaceTokens>("beginToken" to "\${", "endToken" to "}", "tokens" to replaceProperties)
        from(rootProject.file("src/templates"))
        into("build/generated/sources/$modId")
    }
}

fun reobfConfigurationName(configurationName: String) =
    "reobf" + configurationName.replaceFirstChar { it.titlecase() }

val reobfFeatureAttribute: Attribute<String> =
    Attribute.of("dev.lapis256.mekanism_empowered.reobf_feature", String::class.java)

val reobfFeatureByConfigurationName = listOf(
    mainApiSourceSet,
    coreSourceSet,
    coreApiSourceSet,
).flatMap { sourceSet ->
    val featureName = sourceSet.name.replace(".", "-")
    listOf(
        reobfConfigurationName(sourceSet.runtimeElementsConfigurationName) to featureName,
        reobfConfigurationName(sourceSet.apiElementsConfigurationName) to featureName,
    )
}.toMap()

configurations
    .matching { it.name in reobfFeatureByConfigurationName }
    .configureEach {
        attributes.attribute(
            reobfFeatureAttribute,
            reobfFeatureByConfigurationName.getValue(name)
        )
    }

fun setupJarTask(
    modName: String,
    renameFile: Boolean,
    task: TaskProvider<Jar>,
    classifier: String? = null,
    sourceSet: SourceSet,
    vararg sourceSets: SourceSetOutput
) {
    val cleanModName = modName.replace(" ", "").replace(":", "")
    val newName = "$cleanModName-$mcVersion-${project.version}.jar"

    task {
        manifest {
            attributes(
                "Specification-Title" to modName,
                "Specification-Vendor" to Constants.Mod.AUTHOR,
                "Specification-Version" to version,
                "Implementation-Title" to cleanModName,
                "Implementation-Version" to version,
                "Implementation-Vendor" to Constants.Mod.AUTHOR,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                "Timestamp" to System.currentTimeMillis(),
                "Built-On-Java" to "${System.getProperty("java.vm.VERSION")} (${System.getProperty("java.vm.vendor")})",
                "Built-On-Minecraft" to mcVersion,
                // 【合并改动】core 的 mixin 配置现在同处唯一的主 jar，两条 MixinConfigs 都需要声明
                "MixinConfigs" to "$modId.mixins.json,${modId}_core.mixins.json"
            )
        }

        archiveClassifier.set(classifier)
        if (renameFile) {
            archiveFileName.set(newName)
        }
        from(*sourceSets)
    }

    // 主源码集的 jar 由 MDG legacyforge 自动接管重混淆（内部就是按 main 源码集处理的），
    // 这里只处理 apiJar 这类自建 jar，必须显式给出重混淆用的源码集。
    if (sourceSet.name != "main") {
        obfuscation {
            reobfuscate(task, sourceSet) {
                dependsOn("compileJava")

                if (renameFile) {
                    archiveFileName.set(newName)
                }
            }
        }
    }
}

val baseDependencies = listOf(
    ModDep("forge", libs.versions.forge..<"48.0"),
    ModDep("minecraft", mcVersion.eq()),
    ModDep("kotlinforforge", kffVersion.gte()),
    ModDep("mekanism", "10.4.16".gte(), ordering = Order.AFTER),
)
// 【合并改动】单一 mod：不再声明对独立 mekanism_empowered_core 的依赖（core 已并入本体）
val mainModDependencies = baseDependencies.toMutableList().apply {
    add(ModDep.optional("evolvedmekanism", "1.2.1".gte()))
    add(ModDep.optional("mekanismelements", "2.3".gte()))
    add(ModDep.optional("mekanism_extras", "1.5.0".gte()))
    add(ModDep.optional("mekmm", "1.2.1".gte()))
    add(ModDep.optional("emextras", "1.4.1".gte()))
    add(ModDep.incompatible("mekanismtweaks"))
    add(ModDep.incompatible("mekanismupgradesreborn"))
    add(ModDep.incompatible("mekanism_unleashed"))
}

// 【合并改动】只生成一份 mods.toml（模板 src/templates/META-INF/mods.toml）
setupMetaDataTask(modId, Constants.Mod.NAME, generateModMetadata, mainModDependencies)

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = jdkVersion
    }

    java {
        withSourcesJar()
        toolchain {
            languageVersion = JavaLanguageVersion.of(jdkVersion)
            vendor = jvmVendor
        }
        JavaVersion.toVersion(jdkVersion).let {
            sourceCompatibility = it
            targetCompatibility = it
        }
    }

    kotlin {
        jvmToolchain(jdkVersion)

        compilerOptions {
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }

    processResources {
        dependsOn(generateModMetadata)
    }

    named<Jar>("sourcesJar") {
        dependsOn(classes, "mainApiClasses", "coreClasses", "coreApiClasses")

        from(
            mainApiSourceSet.kotlin,
            coreSourceSet.kotlin,
            coreApiSourceSet.kotlin,
        )
    }

    // 【合并改动】唯一的发行 jar：同时打包 core / core.api 的输出（含 core 的 mixin 配置、
    // 访问转换器与服务文件），产物名 MekanismEmpoweredUnleashed-<mcVersion>-<version>.jar
    setupJarTask(
        Constants.Mod.NAME,
        true,
        jar,
        null,
        mainSourceSet,
        mainSourceSet.output,
        mainApiSourceSet.output,
        coreSourceSet.output,
        coreApiSourceSet.output,
    )

    setupJarTask(
        Constants.Mod.NAME,
        false,
        register<Jar>("apiJar"),
        "api",
        mainApiSourceSet,
        mainApiSourceSet.output,
    )

    build {
        dependsOn("apiJar")
    }

    withType<RunGameTask>().configureEach {
        javaLauncher.set(project.javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(jdkVersion))
            vendor.set(jvmVendor)
        })
        standardInput = System.`in`
    }

    withType<Jar>().configureEach {
        from(rootProject.file("LICENSE")) {
            rename { "LICENSE_${Constants.Mod.ID}" }
        }

        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
