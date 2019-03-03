package be.rottenrei.android.coroutine.scopes

import android.app.Dialog
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private open class CancelableCoroutineContextProperty<T>(private val context: CoroutineContext) :
    ReadOnlyProperty<Any, CoroutineContext> {

    private var job = SupervisorJob()

    internal fun cancelJobs() {
        synchronized(job) {
            job.cancel()
            job = SupervisorJob()
        }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): CoroutineContext {
        return job + context
    }

}

/**
 * [ReadOnlyProperty] for a [CoroutineContext] whose child coroutines are cancelled when the [LifecycleOwner] is
 * destroyed. The returned property's [CoroutineContext] is the given [context] but with a different [Job].
 *
 * Example:
 *
 *     override val coroutineContext by cancelOnDestroy(Dispatchers.Main)
 **/
fun LifecycleOwner.cancelOnDestroy(context: CoroutineContext = EmptyCoroutineContext): ReadOnlyProperty<Any, CoroutineContext> =
    CancelableCoroutineContextProperty<LifecycleOwner>(context).also { property ->
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                property.cancelJobs()
            }
        })
    }

/**
 * [ReadOnlyProperty] for a [CoroutineContext] whose child coroutines are cancelled when the [LifecycleOwner] is
 * stopped. The returned property's [CoroutineContext] is the given [context] but with a different [Job].
 *
 * Example:
 *
 *     override val coroutineContext by cancelOnStop(Dispatchers.Main)
 **/
fun LifecycleOwner.cancelOnStop(context: CoroutineContext = EmptyCoroutineContext): ReadOnlyProperty<Any, CoroutineContext> =
    CancelableCoroutineContextProperty<LifecycleOwner>(context).also { property ->
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                property.cancelJobs()
            }
        })
    }

/**
 * [ReadOnlyProperty] for a [CoroutineContext] whose child coroutines are cancelled when the [LifecycleOwner] is
 * paused. The returned property's [CoroutineContext] is the given [context] but with a different [Job].
 *
 * Example:
 *
 *     override val coroutineContext by cancelOnPause(Dispatchers.Main)
 **/
fun LifecycleOwner.cancelOnPause(context: CoroutineContext = EmptyCoroutineContext): ReadOnlyProperty<Any, CoroutineContext> =
    CancelableCoroutineContextProperty<LifecycleOwner>(context).also { property ->
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                property.cancelJobs()
            }
        })
    }

/**
 * [ReadOnlyProperty] for a [CoroutineContext] whose child coroutines are cancelled when the [Dialog] is
 * dismissed. The returned property's [CoroutineContext] is the given [context] but with a different [Job].
 *
 * Example:
 *
 *     override val coroutineContext by cancelOnDismiss(Dispatchers.Main)
 **/
fun Dialog.cancelOnDismiss(context: CoroutineContext = EmptyCoroutineContext): ReadOnlyProperty<Any, CoroutineContext> =
    CancelableCoroutineContextProperty<Dialog>(context).also { property ->
        setOnDismissListener {
            property.cancelJobs()
        }
    }

private class ViewCoroutineContextProperty(private val view: View, context: CoroutineContext) :
    CancelableCoroutineContextProperty<View>(context) {

    override fun getValue(thisRef: Any, property: KProperty<*>): CoroutineContext {
        if (view.isInEditMode) {
            // Coroutines don't work in edit mode. E.g. Dispatchers.Main isn't set. So return an empty
            // context instead
            return EmptyCoroutineContext
        }
        return super.getValue(thisRef, property)
    }

}

/**
 * [ReadOnlyProperty] for a [CoroutineContext] whose child coroutines are cancelled when the [View] is detached from
 * its window. The returned property's [CoroutineContext] is the given [context] but with a different [Job].
 *
 * Example:
 *
 *     override val coroutineContext by cancelOnDetach(Dispatchers.Main)
 **/
fun View.cancelOnDetach(context: CoroutineContext = EmptyCoroutineContext): ReadOnlyProperty<Any, CoroutineContext> =
    ViewCoroutineContextProperty(this, context).also { property ->
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(view: View?) {
                property.cancelJobs()
            }

            override fun onViewAttachedToWindow(view: View?) {
                // not needed
            }
        })
    }
