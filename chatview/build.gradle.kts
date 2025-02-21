plugins {
    id("com.android.library")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.kotlin.android")
}

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

val versionName = project.property("chatviewVersionName").toString()

android {
    namespace = project.property("flavor-chatview-webim").toString()
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api(project(":webimclientsdkandroid"))
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.7.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    testImplementation("junit:junit:4.13.2")
    implementation("com.github.thijsk:TouchImageView:v1.3.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

val siteUrl = project.property("websiteUrl").toString()
val gitUrl = project.property("gitUrl").toString()
val pubUsername = project.property("ossrhUsername").toString()
val pubPassword = project.property("ossrhPassword").toString()

publishing {
    publications {
        register<MavenPublication>("release") {

            groupId = "ru.webim.sdk"
            artifactId = "chatview"
            version = versionName

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("Webim Mobile ChatView for Android")
                description.set("Webim ChatView helps to add functional chat view component with flexibly customizable UI powered by Webim SDK")
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
