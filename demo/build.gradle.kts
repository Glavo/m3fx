plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
    options.encoding = "UTF-8"
}

application {
    mainClass = "org.glavo.m3fx.demo.M3FXDemoLauncher"
}
