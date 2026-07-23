pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TelegramWallet"
include(":app")
include(":core:network")
include(":data:wallet")
include(":domain:wallet")
include(":feature:home")
include(":data:aml")
include(":domain:aml")
include(":data:contract")
include(":domain:contract")
include(":data:transfer")
include(":domain:transfer")
include(":domain:user")
include(":data:user")
include(":core:security")
include(":core:crypto")
include(":data:config")
include(":domain:config")
include(":core:tron")
include(":domain:security")
include(":data:market")
include(":domain:market")
include(":core:database")
include(":core:common")
include(":core:ui")
