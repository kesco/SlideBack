package com.kesco.adk.rx

import rx.Scheduler
import rx.Subscription
import rx.functions.Action0
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

public class WorkThreadScheduler(val executor: Executor) : Scheduler() {

    override fun createWorker(): Scheduler.Worker = ThreadWorker(executor)

    class ThreadWorker(val executor: Executor) : Scheduler.Worker() {
        override fun unsubscribe() {
            throw UnsupportedOperationException()
        }

        override fun isUnsubscribed(): Boolean {
            throw UnsupportedOperationException()
        }

        override fun schedule(action: Action0?): Subscription? {
            throw UnsupportedOperationException()
        }

        override fun schedule(action: Action0?, delayTime: Long, unit: TimeUnit?): Subscription? {
            throw UnsupportedOperationException()
        }
    }
}
