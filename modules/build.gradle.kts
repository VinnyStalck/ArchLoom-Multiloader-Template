plugins {
    java
}

allprojects {
    apply(plugin = "java")

    java.withSourcesJar()
}