# Ubuntu PRoot and Root Chroot

Intelli keeps its existing Ubuntu PRoot environment. PRoot is a userspace compatibility layer and **is not a genuine root chroot**.

## Ubuntu root chroot

The Device Access screen can install Ubuntu Base 24.04.5 for arm64 into Intelli's private files area. The archive URL is the official Ubuntu cdimage release. Intelli checks the archive against a pinned SHA-256 before asking `su` to extract it. The hash is pinned from Ubuntu's `SHA256SUMS`; the release also publishes `SHA256SUMS.gpg`. [Ubuntu Base release files](https://cdimage.ubuntu.com/ubuntu-base/releases/24.04/release/) · [SHA256SUMS](https://cdimage.ubuntu.com/ubuntu-base/releases/24.04/release/SHA256SUMS).

Installation, backup, restore, and removal require an explicit user action and root-manager authorization. Backups stay in Intelli's private app storage and include a SHA-256 sidecar; restore verifies it and stages/validates the rootfs before replacing the active one. Each command runs in a mount namespace created with `unshare -m`; Intelli bind-mounts `/dev`, `/proc`, and `/sys` for that command and cleans them up on exit. The chroot provider requires a supported `su`, arm64, and a kernel/root manager that permits mount namespaces. A failure is reported; the PRoot provider is never used as a fallback.

The root chroot currently runs individual commands from the Device Access console. It does not provide an interactive PTY, shared-storage bind management, or persistent start/stop sessions. Do not mistake it for a tested environment on every rooted OEM build.
