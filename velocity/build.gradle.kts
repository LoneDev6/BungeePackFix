plugins {
    java
}

group = "dev.lone.bungeepackfix"
version = "1.0.8"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.2-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.2-SNAPSHOT")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(16))

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    destinationDirectory.set(project.file("../jars"))
    archiveFileName.set("${rootProject.name}-${project.name}-${project.version}.jar")
}
