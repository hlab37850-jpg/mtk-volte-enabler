package com.volte.mtkenabler.repository

import android.util.Log
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CommandResult(
    val isSuccess: Boolean,
    val out: List<String>,
    val err: List<String>,
    val code: Int
)

class RootRepository {
    private val TAG = "RootRepository"

    private fun isShellInitialized(): Boolean {
        return try {
            Shell.getShell() != null
        } catch (e: Exception) {
            Log.e(TAG, "Shell not initialized", e)
            false
        }
    }

    suspend fun checkRootAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isShellInitialized()) {
                Log.w(TAG, "Shell not initialized, attempting to initialize")
                try {
                    Shell.getShell()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize shell", e)
                    return@withContext false
                }
            }

            val result = Shell.cmd("id").exec()
            val isRooted = result.isSuccess && result.out.any { it.contains("uid=0") }
            Log.d(TAG, "Root check result: $isRooted")
            isRooted
        } catch (e: Exception) {
            Log.e(TAG, "Root check failed", e)
            false
        }
    }

    suspend fun executeCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        try {
            if (!isShellInitialized()) {
                return@withContext CommandResult(false, emptyList(), listOf("Shell not initialized"), -1)
            }

            Log.d(TAG, "Executing: $command")
            val res = Shell.cmd(command).exec()
            CommandResult(res.isSuccess, res.out, res.err, res.code)
        } catch (e: Exception) {
            Log.e(TAG, "Command execution failed: $command", e)
            CommandResult(false, emptyList(), listOf(e.message ?: "Unknown error"), -1)
        }
    }

    suspend fun executeSuCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        try {
            if (!isShellInitialized()) {
                return@withContext CommandResult(false, emptyList(), listOf("Shell not initialized"), -1)
            }

            Log.d(TAG, "Executing SU: $command")
            val res = Shell.su(command).exec()
            CommandResult(res.isSuccess, res.out, res.err, res.code)
        } catch (e: Exception) {
            Log.e(TAG, "SU command execution failed: $command", e)
            CommandResult(false, emptyList(), listOf(e.message ?: "Unknown error"), -1)
        }
    }

    suspend fun readFile(path: String): String? = withContext(Dispatchers.IO) {
        try {
            if (!isShellInitialized()) return@withContext null
            
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
            if (!isShellInitialized()) return@withContext false
            
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
            if (!isShellInitialized()) return@withContext false
            
            val result = Shell.su("[ -f $path ] && echo 'exists' || echo 'not exists'").exec()
            result.isSuccess && result.out.firstOrNull()?.contains("exists") == true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check file existence: $path", e)
            false
        }
    }

    suspend fun backupFile(sourcePath: String, backupPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isShellInitialized()) return@withContext false
            
            val result = Shell.su("cp -p $sourcePath $backupPath").exec()
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Failed to backup file: $sourcePath", e)
            false
        }
    }

    suspend fun restoreFile(backupPath: String, targetPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isShellInitialized()) return@withContext false
            
            val result = Shell.su("cp -p $backupPath $targetPath").exec()
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore file: $backupPath", e)
            false
        }
    }

    suspend fun remountSystem(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isShellInitialized()) return@withContext false
            
            val result = Shell.su("mount -o rw,remount /system").exec()
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remount system", e)
            false
        }
    }

    suspend fun remountVendor(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isShellInitialized()) return@withContext false
            
            val result = Shell.su("mount -o rw,remount /vendor").exec()
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remount vendor", e)
            false
        }
    }
}
