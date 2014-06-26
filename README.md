gradle-sysdeo-tomcat-plugin
===========================

Gradle plugin for generating ".tomcatplugin" artifact for [Sysdeo Eclipse Tomcat Launcher Plugin](http://www.eclipsetotale.com/tomcatPlugin.html)

Configuring DevLoader classpath manually is a pain. This plugin aims to make it easy by using information from your project dependencies.

Usage
=====

In your `build.gradle`:

``` groovy
apply plugin: 'eclipse'
apply plugin: 'eclipse-tomcat'

buildscript {
    repositories {
        maven { url 'http://jcenter.bintray.com' }
    }
    dependencies {
        classpath 'com.anjlab:gradle-sysdeo-tomcat-plugin:1.0.0'
    }
}
```

`.tomcatplugin` will be generated automatically when you run `gradle eclipse` on your project.

Configuring
===========

Configuration is done via `eclipseTomcat` closure.

### Basic Settings

Defaults:

``` groovy
eclipseTomcat {
    rootDir "src/main/webapp"
    exportSource false
    reloadable true
    redirectLogger true
    updateXml true
    warLocation ""
    webPath "ROOT"
}
```

### Classpath

Usually you don't have to do anything special for configuring DevLoader classpath.

You may add custom classpath entries via `includeClasspath` property (which is a List of strings).

You can also exclude any existing classpath entries via `excludeClasspath`, by putting names of those entries there.

By default project will add your project's `bin` directory to classpath via `includeClasspath`.

Also if you have multimodule project, plugin will exclude JARs of your subprojects and include `bin` directories of all subprojects instead. Plugin expects that all your subprojects are also loaded to the same Eclipse workspace.

For example:
``` groovy
eclipseTomcat {
    //  This will exclude anjlab-tapestry-commons-x.y.z.jar
    excludeClasspath.addAll (["anjlab-tapestry-commons"])
    //  This will add output of 'anjlab-tapestry-commons' workspace project to classpath
    includeClasspath.addAll (["/anjlab-tapestry-commons/bin"])
}
```

### Extra Information

``` groovy
eclipseTomcat {
    extraInfo {
        Loader(delegate: true)
    }
}
```

`extraInfo` closure defines XML for Sysdeo's Extra Info property using groovy XML syntax.
This piece of XML will be copied "as is" to the [Tomcat context](http://tomcat.apache.org/tomcat-7.0-doc/config/context.html) for your app.

For example the above snippet will generate this XML:
``` xml
<Loader delegate="true" />
```

One possible usecase when you want to extend `extraInfo` is declaring JNDI resource (like JDBC DataSource) in the context of your application:
```
eclipseTomcat {
    extraInfo {
        Resource(
            name: "jdbc/dbName",
            type: "javax.sql.DataSource",
            auth: "Container",
            maxActive: "100", maxIdle: "30", maxWait: "10000",
            driverClassName: "org.postgresql.Driver",
            url: "jdbc:postgresql://localhost/dbName",
            username: System.getProperty('user.name'))

        Loader(delegate: true)
    }
}
```

License
=======

Copyright 2013 AnjLab

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
