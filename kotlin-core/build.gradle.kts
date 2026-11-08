plugins {
    id("org.jetbrains.kotlin.jvm")
    id("java")
}

group = "it.bwlab.gree"
version = "1.0.0"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")
    implementation("org.json:json:20231013")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.8.10")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
