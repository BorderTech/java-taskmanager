package com.github.bordertech.taskmanager;

import java.io.Serializable;
import java.util.concurrent.Future;

/**
 * Web applications require a {@link Future} implementation that can be serializable to allow the user session to be
 * serialized.
 *
 * @param <T> the future get result type
 * @author Jonathan Austin
 * @since 1.0.0
 */
public interface TaskFuture<T> extends Future<T>, Serializable {

}
