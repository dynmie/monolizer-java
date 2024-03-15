plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.dynmie.monolizer"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.bytedeco:javacv:1.5.10")
    implementation("org.bytedeco:ffmpeg-platform:6.1.1-1.5.10")
    implementation("org.jline:jline-terminal-jni:3.25.1")
}

tasks {
    test {
        useJUnitPlatform()
    }
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Main-Class"] = "me.dynmie.monolizer.MonoMain"
        }
        from(configurations.runtimeClasspath.map { it -> it.map { if (it.isDirectory) it else zipTree(it) } })
    }
}