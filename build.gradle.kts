plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.3"
}

group = "me.dynmie.monolizer"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("org.bytedeco:javacv:1.5.13")
    implementation("org.bytedeco:ffmpeg-platform:8.0.1-1.5.13")
    implementation("org.jline:jline-terminal-jni:3.30.15")
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