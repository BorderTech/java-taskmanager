# Task Master
Task Master helps run ASYNC tasks.

## Status
[![Build Status](https://travis-ci.com/BorderTech/java-taskmaster.svg?branch=master)](https://travis-ci.com/BorderTech/java-taskmaster)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/83bcfdba5e34433894e8b958bdb958a5)](https://www.codacy.com/app/BorderTech/java-taskmaster?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=BorderTech/java-taskmaster&amp;utm_campaign=Badge_Grade)
[![Javadocs](https://www.javadoc.io/badge/com.github.bordertech.taskmaster/taskmaster.svg)](https://www.javadoc.io/doc/com.github.bordertech.taskmaster/taskmaster)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.bordertech.taskmaster/taskmaster.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.bordertech.taskmaster%22%20AND%20a:%22taskmaster%22)

## What is TaskMaster

Task Master allows a Runnable task to be submitted for execution and returns a Future representing that task. The
Future's `get` method will return the given result upon successful completion.

As Web applications require a `Future` implementation that can be serializable, the Task Master has a custom
interface of `TaskFuture` that implements both Future and Serializable. It does not make sense for a `Future`
 to be serilaizable as it is running on a specific thread on a particular server. To allow a Web Application to keep a
reference to the Future, the default implementation of TaskFuture (ie TaskFutureWrapper) wraps the future by
putting the `Future` on a cache and holding onto the cache key that is serializable.

## Cache Helper (JSR107)
The `CacheHelper` allows projects to provide a specific mechanism for creating their cache requirements.

## ServiceHelper
The `ServiceHelper` class helps applications submit ASYNC service calls.
