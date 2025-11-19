package com.pixnpunk.utils

object ThreadUtil {
    /**
     * @return `true` if the current thread is `thread`, otherwise `false`
     */
    fun isSameThread(thread: Thread): Boolean {
        return Thread.currentThread() == thread
    }
}
