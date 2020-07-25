# Xray Reporter
**Xray Reporter** is a Cucumber plugin that helps updating Test Run status to Xray.

Before running a test case, Xray Reporter will set the test run status to `EXECUTING`.
After running the test case, it will set the status to the appropriate status `PASS`/`FAIL`.

## Installation

### Maven

**Step 1.** Add JitPack to your repositories

```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

**Step 2.** Add Xray Status Updater to your dependencies

```
<dependency>
    <groupId>com.github.ltpquang</groupId>
    <artifactId>xray-reporter</artifactId>
    <version>${version}</version>
</dependency>
```

### Gradle

**Step 1.** Add JitPack to your repositories

```
allprojects {
	repositories {
        ...
        maven { url 'https://jitpack.io' }
	}
}
```

**Step 2.** Add Xray Status Updater to your dependencies

```
dependencies {
    implementation "com.github.ltpquang:xray-reporter:$version"
}
```

## Usage

Tell Cucumber to use this plugin by specifying fully qualified class name via `@CucumberOptions` (or anyway you want)

```java
@CucumberOptions(
    plugin = {
        "com.ltpquang.cucumber.plugin.XrayReporter:https://username:password@foosite.com"
    }
)
public class CucumberTest extends CucumberBaseTest {}
```
