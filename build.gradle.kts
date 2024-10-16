plugins {
    kotlin("jvm") version "1.8.20"
    application
}

group = "ru.spbu.math-cs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val osName = System.getProperty("os.name")
val targetOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
var targetArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}

val target = "${targetOs}-${targetArch}"

var version = "0.0.0-SNAPSHOT"
if (project.hasProperty("skiko.version")) {
    version = project.properties["skiko.version"] as String
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.4.1")
    implementation("org.jetbrains.skiko:skiko:0.6.7")

    implementation("org.jetbrains.skiko:skiko-jvm-runtime-$target:$version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.5.1")
    implementation("org.openjfx:javafx-controls:16:mac")
    implementation("org.openjfx:javafx-fxml:16:mac")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.8.0")
    implementation ("com.opencsv:opencsv:5.5.2")



    //implementation("org.jetbrains.skija:skija-windows:${version}\n" +
    //         "org.jetbrains.skija:skija-linux:${version}")
    //implementation("org.jetbrains.skija:skija-macos-x64:${version}")
    //implementation("org.jetbrains.skija:skija-macos-arm64:${version}")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}