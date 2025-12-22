plugins {
    java
    application
    jacoco
}

group = "de.famst"
version = "1.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://www.dcm4che.org/maven2")
    }
    // repository for weasis
    maven {
        url = uri("https://raw.githubusercontent.com/nroduit/mvn-repo/refs/heads/master")
    }
}

val jfreeVersion = "3.4.4"

val dcm4cheVersion = "5.34.1"

val ioVersion = "2.21.0"
val cliVersion = "1.11.0"

val slf4jVersion = "2.0.17"
val logbackVersion = "1.5.23"

val junit5Version = "6.0.1"
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


testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}


tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}


application {
    mainClass.set("de.famst.dicom.visualizer.Main")
}

