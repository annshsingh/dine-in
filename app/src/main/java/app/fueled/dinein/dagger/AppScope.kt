package app.fueled.dinein.dagger

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import javax.inject.Scope

/**
 * @author Annsh Singh
 *
 * A custom scope to let dagger know that all the instances are created
 * for our application and to let dagger know that only one instance
 * is required throughout the application
 */

@Scope
@Retention(RetentionPolicy.CLASS)
annotation class AppScope