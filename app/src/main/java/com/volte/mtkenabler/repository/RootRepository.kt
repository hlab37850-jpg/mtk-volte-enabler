package com.volte.mtkenabler.repository

import android.util.Log
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RootRepository {
    private val TAG = "RootRepository"

    suspend fun checkRootAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("id").exec()
            val isRooted = result.isSuccess && result.out.any { it.contains("uid=0") }
            Log.d(TAG, "Root check result: $isRooted")
            isRooted
        } catch (e: Exception) {
            Log.e(TAG, "Root check failed", e)
            false
        }
    }

    suspend fun executeCommand(command: String): Shell.Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Executing: $command")
            Shell.cmd(command).exec()
        } catch (e: Exception) {
            Log.e(TAG, "Command execution failed: $command", e)
            Shell.Result(null, listOf(), listOf(e.message ?: "Unknown error"), -1, false)
        }
    }

    suspend fun executeSuCommand(command: String): Shell.Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Executing SU: $command")
            Shell.su(command).exec()
        } catch (e: Exception) {
            Log.e(TAG, "SU command execution failed: $command", e)
            Shell.Result(null, listOf(), listOf(e.message ?: "Unknown error"), -1, false)
        }
    }

    suspend fun readFile(path: String): String? = withContext(Dispatchers.IO) {
        try {
            val result = Shell.su("cat $path").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                result.out.joinToString("\n")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read file: $path", e)
            null
        }
    }

    suspend fun writeFile(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val escapedContent = content.replace("\"", "\\\"")
            val result = Shell.su("echo \"$escapedContent\" > $path").exec()
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write file: $path", e)
            false
        }
    }

    suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = Shell.su("[ -f $path ] && echo 'exists' || echo 'not exists'").exec()
            result.isSuccess && result.out.firstOrNull()?.contains("exists") == true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check file existence: $path", e)
            false
        }
    }

    suspend fun backupFile(sourcePath: String, backupPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = Shell.su("cp -p $sourcePath $backupPath").exec()
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Failed to backup file: $sourcePath", e)
            false
        }
    }

    suspend fun restoreFile(backupPath: String, targetPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = Shell.su("cp -p $backupPath $targetPath").exec()
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore file: $backupPath", e)
            false
        }
    }

    suspend fun remountSystem(): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = Shell.su("mount -o rw,remount /system").exec()
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remount system", e)
            false
        }
    }

    suspend fun remountVendor(): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = Shell.su("mount -o rw,remount /vendor").exec()
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remount vendor", e)
            false
        }
    }
}
