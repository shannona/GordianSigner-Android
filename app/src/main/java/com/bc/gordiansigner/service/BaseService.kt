package com.bc.gordiansigner.service

import com.bc.gordiansigner.service.storage.FileStorageApi
import com.bc.gordiansigner.service.storage.SharedPrefApi

abstract class BaseService(
    protected val sharedPrefApi: SharedPrefApi,
    protected val fileStorageApi: FileStorageApi
)