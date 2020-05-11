# ni-http-suite

[![Maven Central](https://img.shields.io/maven-central/v/ch.digitalfondue.ni-http-suite/ni-http-suite.svg)](https://search.maven.org/search?q=g:ch.digitalfondue.ni-http-suite%20a:ni-http-suite)

or Not Invented Here ttp-suite.


It's a minimalistic set of libraries collated together for making small throw away http demo.

The main goal is to be able to generate a graal native-image with 0 additional descriptors (especially the reflection one).

You can see a demo here:  https://github.com/digitalfondue/ni-httpsuite-example

Based on:

 - http server from the  apache [httpcomponents-core libraries](https://hc.apache.org/httpcomponents-core-5.0.x/index.html).
 - json read/write from the eclipse [json processing api](https://eclipse-ee4j.github.io/jsonp/).
 
## Download

maven:

```xml
<dependency>
    <groupId>ch.digitalfondue.ni-http-suite</groupId>
    <artifactId>ni-http-suite</artifactId>
    <version>0.0.1</version>
</dependency>
```

gradle:

```
compile 'ch.digitalfondue.ni-http-suite:ni-http-suite:0.0.1'
```

 
## License

ni-http-suite is licensed under the Apache License Version 2.0.

## Example

See https://github.com/digitalfondue/ni-http-suite/blob/master/src/test/java/ch/digitalfondue/nihttpsuite/Example.java 
