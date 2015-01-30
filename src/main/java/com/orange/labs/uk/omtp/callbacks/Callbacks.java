/*
 * Copyright (C) 2012 Orange Labs UK. All Rights Reserved.
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.orange.labs.uk.omtp.callbacks;

import android.app.Activity;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Factory methods for use with {@link Callback} instances.
 */
@ThreadSafe
public final class Callbacks {
    /**
     * Wraps a given callback so that it is executed by the given executor.
     * <p>
     * This is helpful if you need to provide a callback to a service whose thread execution policy
     * you do not know or control, but whose callbacks need to be executed in a given thread or with
     * a given thread policy. Most often this is used to make sure that callbacks happen only in the
     * ui thread, for manipulating ui elements.
     * <p>
     * See also {@link #uiThreadCallback(Activity, Callback)}, a further convenience method.
     */
    public static <T> Callback<T> wrap(final Executor executor, final Callback<T> delegate) {
        return new Callback<T>() {
            @Override
            public void onFailure(final Exception error) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        delegate.onFailure(error);
                    }
                });
            }

            @Override
            public void onSuccess(final T result) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        delegate.onSuccess(result);
                    }
                });
            }
        };
    }

    /**
     * The single global instance of the empty callback.
     * <p>
     * This is without type parameter, because we want to re-use the same single instance for any
     * type of operation.
     */
    // This unchecked warning is safe, this callback can be used with any type.
    @SuppressWarnings("rawtypes")
    private static final Callback EMPTY_CALLBACK = new Callback() {
        @Override
        public void onFailure(Exception error) {
            // Ignored.
        }

        @Override
        public void onSuccess(Object result) {
            // Ignored.
        }
    };

    /**
     * Convenience method to get a callback for an operation whose result you wish to ignore.
     */
    // This unchecked warning is safe, this callback can be used with any type.
    @SuppressWarnings("unchecked")
    public static <T> Callback<T> emptyCallback() {
        return EMPTY_CALLBACK;
    }
    
    /**
     * Gets an Executor whose execution policy is to run Runnables on the Activity's ui thread.
     */
    private static Executor asUiThreadExecutor(final Activity activity) {
        return new Executor() {
            @Override
            public void execute(Runnable command) {
                activity.runOnUiThread(command);
            }
        };
    }

    /**
     * Convenience factory method to wrap a callback to happen on an Activity's ui thread.
     * <p>
     * For example in your Activity, instead of this:
     *
     * <pre class="code">
     * myService.doSomething(input, new Callback&lt;Void&gt;() {
     *     &#064;Override
     *     onSuccess() {
     *         modifyButton();
     *     }
     *     // Failure omitted...
     * });
     * </pre>
     *
     * which will cause you grief if {@code myService} uses background threads, use this:
     *
     * <pre class="code">
     * myService.doSomething(input, Callbacks.uiThreadCallback(this, new Callback&lt;Void&gt;() {
     *     &#064;Override
     *     onSuccess() {
     *         modifyButton();
     *     }
     *     // Failure omitted...
     * }));
     * </pre>
     */
    public static <T> Callback<T> uiThreadCallback(final Activity activity, Callback<T> delegate) {
        return wrap(asUiThreadExecutor(activity), delegate);
    }
}
