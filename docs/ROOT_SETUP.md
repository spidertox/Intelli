# Root Setup

Root is optional and depends on a supported `su` manager and device configuration. Intelli checks for common `su` paths without requesting authorization in the background. The first confirmed Root Shell command may trigger the manager's authorization prompt.

The Device Access console displays the exact shell text before execution. Root commands are never automatically chosen for an ADB, Shizuku, terminal, or AI command failure. Intelli does not save a root password or run hidden root commands. Root execution logs must not capture command text that may contain credentials.

Root access can expose or change the whole device. Do not run commands you do not understand. Android 10+ OEM builds vary; `su` presence alone does not prove authorization or that a requested operation is supported. A denied `su` process is reported as a failed command.
