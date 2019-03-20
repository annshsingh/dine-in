package app.fueled.dinein.dagger

import dagger.Module

/**
 * @author Annsh Singh
 *
 * This module consists of all the repositories listed in this app and
 * does the work of providing instances of them
 */

@Module(includes = [(StorageModule::class), (NetworkModule::class)])
class RepositoryModule {

    //List your repositories here

}