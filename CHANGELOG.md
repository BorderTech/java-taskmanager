# Change log

## Release in-progress

* Latest qa-parent
* Switch from travis-ci to GitHub Actions #45
* Latest dependencies
* Extract taskmaster-servlet-tools into its own repo java-servlet-tools. Projects using servlet-tools will need to add the new dependency as it is no longer provided by taskmaster-core #47

## 2.0.0-beta-1

* Consolidate configuration properties into utility classes.
* TaskFuture result type extends Serializable.
* Refactor ResultHolder into an interface.
* Refactor ServiceHelper API to use submitSync and submitASync methods instead of passing a call type parameter into handle methods.
* ServiceHelper submitASync methods return a TaskFuture.
* New ServiceCacheUtil to provide default caches for service calls.

## 1.0.5

* Latest qa-parent and dependencies

## 1.0.4

* Latest qa-parent and dependencies

## 1.0.3

* Latest qa-parent and dependencies

## 1.0.2

* Switch from circleCI to travis-ci
* Project refactor:
  * Split project into submodules for taskmaster-core, cache-helper and service-helper.
  * Use backing "provider" interfaces for TaskMaster, CachingHelper and ServiceHelper.
  * ResultHolder and ServiceAction generic types now extends Serializable
* Provide ehcache implementation for caching and support XML config.

## 1.0.1

* Ability to run tasks in predefined thread pools
* RejectedTaskException for when TaskMaster could not execute the task.

## 1.0.0

* Initial version
