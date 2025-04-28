plugins {
    id("java")
}

group = "com.github.extraplays"
version = "1.0.9"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")

    implementation("org.xerial:sqlite-jdbc:3.43.2.0")
    implementation("mysql:mysql-connector-java:8.0.33")

    implementation("com.zaxxer:HikariCP:6.3.0")
}

tasks.jar {
    archiveBaseName.set("ZenORM")
    archiveVersion.set(project.version.toString())
}