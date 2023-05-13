plugins {
    kotlin("jvm")
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc.get().outputDirectory.get())
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "at.connyduck"
                artifactId = "networkresult-calladapter"
                version = "1.0.0"

                from(components["java"])
                artifact(sourcesJar.get())
                artifact(javadocJar.get())

                pom {
                    name.set("networkresult-calladapter")
                    description.set("A Retrofit calladapter for custom NetworkResult as return type")
                    url.set("https://github.com/connyduck/networkresult-calladapter")

                    licenses {
                        license {
                            name.set("Apache-2.0 License")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("connyduck")
                            name.set("Konrad Pozniak")
                            email.set("opensource@connyduck.at")
                        }
                    }

                    scm {
                        connection.set("scm:git:github.com/connyduck/networkresult-calladapter.git")
                        developerConnection.set("scm:git:ssh://github.com/connyduck/networkresult-calladapter.git")
                        url.set("https://github.com/connyduck/networkresult-calladapter")
                    }
                }
            }
        }
    }
}

signing {
    val signingKeyId = rootProject.ext["signing.keyId"] as String?
    val signingKey = rootProject.ext["signing.key"] as String?
    val signingPassword = rootProject.ext["signing.password"] as String?
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications)
}

kotlin {
    explicitApi()
    jvmToolchain(8)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    val okHttpVersion = "4.9.3"
    val retrofitVersion = "2.9.0"
    val jUnitVersion = "5.8.2"
    val moshiVersion = "1.13.0"

    api(kotlin("stdlib"))

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.2")

    api("com.squareup.okhttp3:okhttp:$okHttpVersion")

    api("com.squareup.retrofit2:retrofit:$retrofitVersion")

    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")

    testImplementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    testImplementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
}
