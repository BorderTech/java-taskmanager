# Task Manager
Task Manager helps run ASYNC tasks.

Task Manager allows a Runnable task to be submitted for execution and returns a Future representing that task. The
Future's `get` method will return the given result upon successful completion.

As Web applications require a `Future` implementation that can be serializable, the Task Manager has a custom
interface of `TaskFuture` that implements both Future and Serializable. It does not make sense for a `Future`
 to be serilaizable as it is running on a specific thread on a particular server. To allow a Web Application to keep a
reference to the Future, the default implementation of TaskFuture (ie TaskFutureWrapper) wraps the future by
putting the `Future` on a cache and holding onto the cache key that is serializable.

## Cache Helper (JSR107)
The `CacheHelper` allows projects to provide a specific mechanism for creating their cache requirements.

## ServiceHelper
The `ServiceHelper` class helps applications submit ASYNC service calls.
