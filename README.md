Provides convenience extension methods to easily create a `CoroutineScope`
that is tied to the lifecycle of common Android classes (`AppCompatActivity`, `AppCompatFragment`, `Dialog`, `View`).
This allows you to auto-cancel any running coroutines that still have references to the Android framework object,
thus avoiding a memory leak of that reference.

Install via JCenter:

```groovy
implementation 'be.rottenrei:android-coroutine-scopes:LATESTVERSION'
```

[ ![JCenter](https://api.bintray.com/packages/karottenreibe/android-coroutine-scopes/android-coroutine-scopes/images/download.svg) ](https://bintray.com/karottenreibe/android-coroutine-scopes/android-coroutine-scopes/_latestVersion)

# Requirements

You must use `androidx.lifecycle` and `androidx.appcompat` in order for these extensions to be able to listen to lifecycle events of activities and fragments.

# Usage

```kotlin
class MyActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext by cancelOnDestroy()
}

class MyView : View(), CoroutineScope {
    override val coroutineContext by cancelOnDetach()
}
```

All coroutines started on this `CoroutineScope` are cancelled in the corresponding lifecycle event
(e.g. `onDestroy` or `onDetachFromWindow`).

# Available Scopes

- `LifecycleOwner.cancelOnDestroy()` - cancelled when the activity/fragment is destroyed
- `LifecycleOwner.cancelOnStop()` - cancelled when the activity/fragment is stopped
- `LifecycleOwner.cancelOnPause()` - cancelled when the activity/fragment is paused
- `Dialog.cancelOnDismiss()` - cancelled when the dialog is dismissed
- `View.cancelOnDetach()` - cancelled when the view is detached from its window

By default, all of these return a `CoroutineContext` with only a custom `Job` used to cancel
child coroutines. You can pass in a custom context that should be used instead (e.g. `Dispatchers.Main`) as well.

