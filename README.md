# app-logger

It is a logging library that stores logs locally on the android device. I created this library as an alternate for server logging to debug issues. it helped to fix some specific issues that only happened in release build where i did not have access to server logging.

Remove the library usage before publishing on playstore. Its main purpose is to debug specific issues and share the log file. 

[![](https://jitpack.io/v/a914-gowtham/app-logger.svg)](https://jitpack.io/#a914-gowtham/app-logger)

<img src="https://github.com/a914-gowtham/app-logger/blob/master/demo.gif" width="40%" height="40%"/>

Download
--------

```gradle
 // Add it in your root build.gradle at the end of repositories:
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
   	   mavenCentral()
           maven { url 'https://jitpack.io' }
  }
}

// app level build.gradle
dependencies {
   implementation 'com.github.a914-gowtham:app-logger:1.0.0'
}
```

## Usage 

Write logs
```kotlin
    // verbose
    AppLogger.writeLog(context, "Navigation screen verbose" +
                "log time ${System.currentTimeMillis()}")
    // info 
    AppLogger.writeILog(context, "Navigation screen " +
                "log time ${System.currentTimeMillis()}")

    // warn 
    AppLogger.writeWLog(context, "Navigation screen " +
                "log time ${System.currentTimeMillis()}")

    // error 
    AppLogger.writeELog(context, "Navigation screen " +
                "log time ${System.currentTimeMillis()}")
```



View logs
```kotlin
val logViewer = LogViewer(liveLog = false)
logViewer.show(supportFragmentManager, LogViewer.TAG)
```


