# Appium Android and iOS UI testing

This projects is a demo testing framework for validating Android and iOS apps. It uses Cucumber and TestNG for structuring and executing tests. Also, it's configured with Allure reporting framework.

The key feature of this demo is that it can use device, emulator or cloud provider to run tests. The following cloud providers are supported: 
* BrowserStack
* Kobiton
* SauceLabs
* Perfecto

_TODO: AWS Device Farm_

See details for configuring providers below

## Required Tools

1. JDK8
2. Maven


## Basic steps for running tests on device or emulator 

1. Update/add profile to `profiles` node of `pom.xml`
```
        <profile>
            <id>$DEVICE_PROFILE_NAME</id>
            <properties>
                <provider.name>local</provider.name>
                <device.type>device|emulator</device.type>
                <device.platform.name>android|ios</device.platform.name>
                <device.platform.version>$ANDROID_VERSION</device.platform.version>
                <device.name>$DEVICE_NAME</device.name>
            </properties>
        </profile>
``` 
For iOS device you also need to specify `device.udid`
2. Run tests
```
mvn clean test -P$DEVICE_PROFILE_NAME -P$SUITE_PROFILE_NAME
```

## Generate report

You can generate a report using one of the following command:

* `mvn allure:serve`

    Report will be generated into temp folder. Web server with results will start.

* `mvn allure:report`

    Report will be generated tо directory: `target/site/allure-maven/index.html`


## Running tests with cloud providers

1. Signup to cloud device farm service and get your username and access key. 
Set them to `$USERNAME`, `$ACCESS_KEY` 
2. Upload your app. Set path or ID, provided by cloud provider to `$APP` env variable
4. Create new device provile in `pom.xml`
    ```
    <profile>
        <id>$PROVIDER-$PLATFORM-$DEVICE_TYPE</id>
        <properties>
            <provider.name>$PROVIDER</provider.name>
            <device.type>$DEVICE_TYPE</device.type>
            <device.platform.name>$PLATFORM</device.platform.name>
            <device.platform.version>$VERSION</device.platform.version>
            <device.name>$DEVICE_NAME</device.name>
        </properties>
    </profile>
    ```
    where   
    * **$PROVIDER** - _local_, _browserstack_, _saucelabs_, _kobiton_ or _perfecto_
    * **$PLATFORM** - _ios_ or _android_
    * **$DEVICE_TYPE** - _device_ or _emulator_
    * **$VERSION** - OS version (i.e "7.0" or "12.4")
    * **$DEVICE_NAME** - name of emulator or device
5. Run tests
    ```
    USERNAME=... ACCESS_KEY=... APP=...  mvn clean test -P$DEVICE_PROVILE -Psuite-full
    ```
        