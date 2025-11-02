plugins {
    java
    application
}

group = "de.famst"
version = "1.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://www.dcm4che.org/maven2")
    }
}

val jfreeVersion = "3.2"
val dcm4cheVersion = "3.3.8"
val ioVersion = "2.6"
val cliVersion = "1.4"
val slf4jVersion = "1.7.25"
val logbackVersion = "1.2.3"
val junitVersion = "4.11"
val junit5Version = "5.0.1"
val hamcrestVersion = "1.3"

dependencies {
    implementation("org.jfree:jfreesvg:$jfreeVersion")
    implementation("org.dcm4che:dcm4che-core:$dcm4cheVersion")
    implementation("commons-io:commons-io:$ioVersion")
    implementation("commons-cli:commons-cli:$cliVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit5Version")
    testImplementation("org.hamcrest:hamcrest-all:$hamcrestVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
}

application {
    mainClass.set("de.famst.dicom.visualizer.Main")
}

