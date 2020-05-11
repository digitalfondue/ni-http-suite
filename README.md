# ni-http-suite

or Not Invented Here ttp-suite.


It's a minimalistic set of libraries collated together for making small throw away http demo.

The main goal is to be able to generate a graal native-image with 0 additional descriptors (especially the reflection one).

Based on:

 - http server from the  apache [httpcomponents-core libraries](https://hc.apache.org/httpcomponents-core-5.0.x/index.html).
 - json read/write from the eclipse [json processing api](https://eclipse-ee4j.github.io/jsonp/).
 
## License

ni-http-suite is licensed under the Apache License Version 2.0.

## Example

See https://github.com/digitalfondue/ni-http-suite/blob/master/src/test/java/ch/digitalfondue/nihttpsuite/Example.java 
