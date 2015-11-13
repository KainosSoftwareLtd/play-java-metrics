This is small [Play Framework Java](https://www.playframework.com/documentation/2.3.x/JavaHome) project with [Dropwizard metrics](http://metrics.dropwizard.io/3.1.0/) attached to it.
=================================

This is a skeleton Play Framework application which can be used for new projects - it has Dropwizard metrics instrumentation added on top of it.

This can be used as a new application after removal of following code:
* Global.onStart sets RandomProvider
* RandomProvider and Impl
* Integration tests which tests the metrics are not needed and can be replaced with proper application code.

TODO
* Reporting
* Instrumentation of the clients
* HTTP statuses Instrumentation? is this required?

Open for suggestions and comments.
