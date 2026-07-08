#!/bin/bash
# Workspace backup and email script with logging

LOG_DIR="$(dirname "$0")/../logs"
LOG_FILE="$LOG_DIR/backup-$(date +%Y%m%d-%H%M%S).log"
WORKSPACE="/home/work/.openclaw/workspace"
BACKUP_FILE="/home/work/.openclaw/tmp/workspace-backup.tar.gz"
SKILL_DIR="$WORKSPACE/email-sender"

{
  echo "===== Backup Task Started: $(date '+%Y-%m-%d %H:%M:%S') ====="
  echo ""

  # Step 1: Compress
  echo "[1/2] Compressing workspace..."
  mkdir -p /home/work/.openclaw/tmp
  cd "$WORKSPACE"
  tar czf "$BACKUP_FILE" --exclude='.openclaw' --exclude='node_modules' --exclude='.git' --exclude='logs' . 2>&1
  if [ $? -eq 0 ]; then
    SIZE=$(ls -lh "$BACKUP_FILE" | awk '{print $5}')
    echo "  ✅ Compressed successfully: $SIZE"
  else
    echo "  ❌ Compression failed!"
    exit 1
  fi
  echo ""

  # Step 2: Send email
  echo "[2/2] Sending email to stdueg@qq.com..."
  cd "$SKILL_DIR"
  OUTPUT=$(python3 scripts/send_email.py \
    --to "stdueg@qq.com" \
    --subject "📦 工作区备份 - $(date '+%Y-%m-%d %H:%M')" \
    --body "自动备份任务\n\n备份时间：$(date '+%Y-%m-%d %H:%M:%S')\n文件大小：$SIZE\n\n此邮件由定时任务自动发送。" \
    --attach "$BACKUP_FILE" 2>&1)
  RESULT=$?
  echo "  $OUTPUT"
  if [ $RESULT -eq 0 ]; then
    echo "  ✅ Email sent successfully"
  else
    echo "  ❌ Email sending failed (exit code: $RESULT)"
  fi
  echo ""

  echo "===== Backup Task Finished: $(date '+%Y-%m-%d %H:%M:%S') ====="
} 2>&1 | tee "$LOG_FILE"
