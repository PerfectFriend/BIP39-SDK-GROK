# Publishing (JitPack)

## Dependency

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.PerfectFriend:bip39-onboarding-android:1.2.1")
    // multi-module form (if needed):
    // implementation("com.github.PerfectFriend.bip39-onboarding-android:library:1.2.1")
}
```

JitPack resolves the **git tag** `1.2.1` (or `v1.2.1`) on  
https://github.com/PerfectFriend/bip39-onboarding-android

## Create a release tag (required once per version)

```bash
git tag -a 1.2.1 -m "BIP39 Onboarding engine 1.2.1"
git push origin 1.2.1
```

Or on GitHub: **Releases → Draft a new release → tag `1.2.1`**.

Then open:

https://jitpack.io/#PerfectFriend/bip39-onboarding-android/1.2.1

Wait for the green **Get** / successful build, then use the dependency above.

## Local publish (without JitPack)

```bash
./gradlew :library:publishToMavenLocal
```

```kotlin
implementation("com.github.PerfectFriend:bip39-onboarding:1.2.1")
```

(with `mavenLocal()` in repositories)

## Version bumps

1. Update `version` in `library/build.gradle.kts`
2. Update README / CHANGELOG
3. Tag and push
4. Trigger JitPack build
