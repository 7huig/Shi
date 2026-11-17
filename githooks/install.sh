#!/bin/sh
# 启用仓库自带的 Git 钩子。首次克隆后执行一次：
#   sh githooks/install.sh
set -eu

cd "$(git rev-parse --show-toplevel)"

if [ ! -f githooks/pre-commit ]; then
    echo "找不到 githooks/pre-commit" >&2
    exit 1
fi

# Windows 上 chmod 是空操作，但 Git for Windows 会用 sh 执行钩子；
# Linux / macOS 需要可执行位。
chmod +x githooks/pre-commit 2>/dev/null || true

git config core.hooksPath githooks
echo "已启用 githooks/pre-commit：超过 20 KiB 的代码文件会被拒绝提交。"
echo "临时放行：ALLOW_LARGE_CODE_FILES=1 git commit ..."
