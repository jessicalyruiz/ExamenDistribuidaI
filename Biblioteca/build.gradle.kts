plugins {
    id("java")
    id("application") // crear aplicaciones ejecutables.
    id("io.freefair.lombok") version "8.4" //getters, setters
    id("com.github.johnrengelman.shadow") version "8.1.1" //creación de "fat JARs" (JARs que contienen todas las dependencias)

}


group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.hsqldb:hsqldb:2.7.2")
    implementation("org.jboss.weld.se:weld-se-core:5.1.2.Final")//para la inyección de dependencias
    implementation("org.hibernate:hibernate-core:6.4.1.Final")//mapeo objeto-relacional

    implementation("com.h2database:h2:2.2.224") //base de datos embebida
    implementation("com.sparkjava:spark-core:2.9.4")//un marco web ligero
    implementation("com.google.code.gson:gson:2.10.1")//trabajar con JSON



}

tasks.test {
    useJUnitPlatform()
}

sourceSets {//Configura el conjunto de fuentes principal del proyecto y especifica la ubicación del directorio de salida de recursos
    main {
        output.setResourcesDir( file("${buildDir}/classes/java/main") )
    }
}
tasks.jar {//Configura la tarea de construcción del JAR y especifica atributos del manifiesto, como la clase principal y la ruta de clase.
    manifest{
        attributes(
                mapOf("Main-Class" to "com.distribuida.Principal",
                        "Class-Path" to configurations.runtimeClasspath
                                .get()
                                .joinToString(separator = " "){
                                    file->"${file.name}"
                                })
        )
    }
}
