/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import org.mozilla.fpm.models.Backup
import org.mozilla.fpm.utils.CryptUtils
import org.mozilla.fpm.utils.Utils
import org.mozilla.fpm.utils.Utils.Companion.getBackupStoragePath
import org.mozilla.fpm.utils.Utils.Companion.getCryptedStoragePath
import org.mozilla.fpm.utils.Utils.Companion.makeFirefoxPackageContext
import org.mozilla.fpm.utils.ZipUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

@SuppressLint("StaticFieldLeak")
object BackupRepositoryImpl : BackupRepository {
    private const val KILOBYTE: Long = 1024
    private lateinit var ctx: Context

    fun setContext(ctx: Context) {
        this.ctx = ctx
    }

    override fun create(k: String) {
        if (File(getBackupStoragePath(ctx)).mkdirs() || File(getCryptedStoragePath(ctx)).mkdirs()) {
            Log.d(javaClass.name, "Repository initialized!")
        }

        val deployPath = getBackupDeployPath()

        if (deployPath != null) {
            ZipUtils().compress(deployPath, "${getBackupStoragePath(ctx)}/${k}_arch.zip")
            CryptUtils.encrypt(
                FileInputStream("${getBackupStoragePath(ctx)}/${k}_arch.zip"),
                FileOutputStream("${getBackupStoragePath(ctx)}/$k.zip")
            )
            File("${getBackupStoragePath(ctx)}/${k}_arch.zip").delete()
            return
        }
        Log.w(javaClass.name, "No Firefox app installed. Please install one!")
    }

    override fun import(fileUri: Uri, fileName: String) {
        if (File(getBackupStoragePath(ctx)).mkdirs() || File(getCryptedStoragePath(ctx)).mkdirs()) {
            Log.d(javaClass.name, "Repository initialized!")
        }

        val inputStream: InputStream? = ctx.contentResolver.openInputStream(fileUri)
        val copy = File("${getBackupStoragePath(ctx)}/$fileName")
        val outputStream: OutputStream = FileOutputStream(copy)
        inputStream?.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
    }

    override fun deploy(name: String) {
        if (File(getBackupStoragePath(ctx)).mkdirs() || File(getCryptedStoragePath(ctx)).mkdirs()) {
            Log.d(javaClass.name, "Repository initialized!")
        }

        val deployPath = getBackupDeployPath()

        if (deployPath != null) {
            File(deployPath).deleteRecursively()
            CryptUtils.decrypt(
                FileInputStream("${getBackupStoragePath(ctx)}/$name"),
                FileOutputStream("${getCryptedStoragePath(ctx)}/$name")
            )
            ZipUtils().extract("${getCryptedStoragePath(ctx)}/$name", getBackupDeployPath())
            File("${getCryptedStoragePath(ctx)}/$name").delete()
            return
        }
        Log.w(javaClass.name, "No Firefox app installed. Please install one!")
    }

    override fun remove(k: String) {
        File("${getBackupStoragePath(ctx)}/$k").delete()
    }

    override fun update(t: Backup, k: String) {
        File("${getBackupStoragePath(ctx)}/${t.name}").renameTo(File("${getBackupStoragePath(ctx)}/$k"))
    }

    override fun get(k: String): Backup {
        File(getBackupStoragePath(ctx)).listFiles()?.forEach {
            if (it.name == k) {
                return Backup(it.name, Utils.getFormattedDate(it.lastModified()), it.length() / KILOBYTE)
            }
        }

        return Backup("", "", 0)
    }

    override fun getAll(): List<Backup> {
        val backupsList: MutableList<Backup> = arrayListOf()

        if (!File(getBackupStoragePath(ctx)).exists()) {
            File(getBackupStoragePath(ctx)).mkdirs()
        }

        File(getBackupStoragePath(ctx)).listFiles()?.forEach {
            backupsList.add(Backup(it.name, Utils.getFormattedDate(it.lastModified()), it.length() / KILOBYTE))
        }

        return backupsList
    }

    private fun getBackupDeployPath(): String? {
        return makeFirefoxPackageContext(ctx)?.applicationInfo?.dataDir
    }
}
