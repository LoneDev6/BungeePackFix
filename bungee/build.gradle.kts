plugins {
    java
}

group = "dev.lone.bungeepackfix"
version = "1.0.8"

dependencies {
    compileOnly(files("./libs/waterfall-1.19-510.jar"))
}

java.sourceCompatibility = JavaVersion.VERSION_1_8

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    destinationDirectory.set(project.file("../jars"))
    archiveFileName.set("${rootProject.name}-${project.name}-${project.version}.jar")
}
