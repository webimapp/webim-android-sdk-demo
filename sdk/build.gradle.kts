plugins {
    id("com.android.library")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.kotlin.android")
}

val versionName = project.property("sdkVersionName").toString()

android {
    compileSdk = 33
    defaultConfig {
        minSdk = 21
        targetSdk = 33
        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("proguard-rules.pro")
    }
    buildTypes {
    }

    lint {
        abortOnError = false
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.10")
    implementation("net.zetetic:android-database-sqlcipher:4.4.3@aar")
    implementation("androidx.sqlite:sqlite:2.2.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha04")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.7.10")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
}

val siteUrl = project.property("websiteUrl").toString()
val gitUrl = project.property("gitUrl").toString()
val pubUsername = project.property("ossrhUsername").toString()
val pubPassword = project.property("ossrhPassword").toString()

publishing {
    publications {
        register<MavenPublication>("release") {

            groupId = "ru.webim.sdk"
            artifactId = "webimclientsdkandroid"
            version = versionName

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("Webim Mobile SDK for Android")
                description.set("Webim Mobile SDK enables Android developers to implement chats into their mobile apps for communications between users and agents.")
                url.set(siteUrl)
                packaging = "jar"

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("mshengeliia")
                        name.set("Maksim Shengeliia")
                        email.set("mshengeliia@webim-team.ru")
                    }
                    developer {
                        id.set("nkaberov")
                        name.set("Nikita Kaberov")
                        email.set("nkaberov@webim.ru")
                    }
                }
                scm {
                    connection.set(gitUrl)
                    developerConnection.set(gitUrl)
                    url.set(siteUrl)
                }
            }
        }
        repositories {
            maven {
                credentials {
                    username = pubUsername
                    password = pubPassword
                }
                val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (versionName.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            }
        }
    }
}

signing {
    sign(publishing.publications["release"])
}