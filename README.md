This is small Playframework Java project with dropwizard metrics attached to it.
=================================

# Overview
This is skeleton playframework application which can be used for new project - it has dropwizard metrics instrumentation added on top of it.

This can be used as new application after removal of following code:
* Global.onStart sets RandomProvider this is not needed.
* RandomProvider and Impl are not needed
* Integration tests which tests the metrics are not needed and can be replaced with proper application code.

# Running

Repository contains self-contained sbt project. Download and type:

```
./sbt run
```

# How-to

The solution extends behaviour of Global Play Framework class by adding support to @Timed annotation and stores the MetricsRegistry
which can be obtained under /admin/metrics.

Additionaly it provides WSRequestFactory which can be used to create instrumented request. The timings will be available under
clientRequest.{givenName} metrics.

# TODO

* Reporting configuration
* Refactor instrumentation of client
* Add support to count only instrumentation for client

Open for suggestions and comments.